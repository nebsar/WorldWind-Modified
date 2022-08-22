package gov.nasa.worldwind.layers.mercator.tpk;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.mercator.MercatorSector;
import gov.nasa.worldwind.layers.mercator.MercatorTextureTile;
import gov.nasa.worldwind.layers.mercator.ProceduralTiledImageLayer;
import gov.nasa.worldwind.util.LevelSet;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import javax.imageio.ImageIO;

/**
 *
 * @author EagleEye
 */
public class TPKLayer extends ProceduralTiledImageLayer {

    private TPKFile tpkFile;

    public TPKLayer(LevelSet levelSet) {
        super(levelSet);
    }

    public TPKLayer(Object source, String layerName) {
        this(initParams(source, layerName));

        tpkFile = new TPKFile(new File((String) source));
    }

    private static LevelSet initParams(Object source, String layerName) {

        TPKFile file = new TPKFile(new File((String) source));

        AVList params = new AVListImpl();
        long n = file.getMaxZoomLevel();
        System.out.println(n);
        params.setValue(AVKey.SERVICE, "file://" + layerName + "?");
        params.setValue(AVKey.TILE_WIDTH, 256);
        params.setValue(AVKey.TILE_HEIGHT, 256);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/MBTile/".concat(layerName));
        params.setValue(AVKey.DATASET_NAME, "h");
        params.setValue(AVKey.FORMAT_SUFFIX, "." + file.getImageFormat());
        System.out.println(file.getImageFormat());
        params.setValue(AVKey.NUM_LEVELS, (int) file.getMaxZoomLevel() - 2);
        if (file.getMinZoomLevel() - 3 < 0) {
            params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
        } else {
            params.setValue(AVKey.NUM_EMPTY_LEVELS, file.getMinZoomLevel() - 3);
        }
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(22.5d), Angle.fromDegrees(45.0d)));
        params.setValue(AVKey.RETAIN_LEVEL_ZERO_TILES, true);
        params.setValue(AVKey.SECTOR, new MercatorSector(-1.0, 1.0, Angle.NEG180, Angle.POS180));

        file.close();

        return new LevelSet(params);
    }

    @Override
    protected BufferedImage createTileImage(MercatorTextureTile tile, BufferedImage image) {
        int width = tile.getLevel().getTileWidth();
        int height = tile.getLevel().getTileHeight();

        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        BufferedImage bim = getBuffered(tile.getLevelNumber(), tile.getColumn(), tile.getRow());
        g2.drawImage(bim, 0, 0, width, height, null);

        g2.dispose();
        return image;

    }

    /**
     * @param zoomLevel
     * @param x - tile column
     * @param y - tile row
     *
     * @return BufferedImage
     */
    private synchronized BufferedImage getBuffered(int zoomLevel, int column, int row) {
        int zoom = zoomLevel + 3;
        TPKTile tile = null;
        BufferedImage bi = null;
        byte[] tileData = null;
        try {
            TPKZoomLevel tPKZoomLevel = this.tpkFile.getZoomLevelMap().get((long) zoom);
            List<TPKBundle> bundles = tPKZoomLevel.getBundles();
            TPKBundle bundle = bundles.stream().filter(b -> b.inBundle(column, row)).findFirst().orElse(null);
            int bundleIndex = bundles.indexOf(bundle);
            long bundleRow = (tPKZoomLevel.getMax_row_column() - row) - bundle.baseRow;
            switch (tpkFile.getCacheType()) {

                case V1: {
                    long indexReadOffset
                            = 16
                            + (((column - bundle.baseColumn) * 128) + bundleRow)
                            * 5;

                    // read the tile index and get the offset to the tile data
                    long tileDataOffset = ((TPKZoomLevelV1) tPKZoomLevel).getTileDataOffset(bundle, indexReadOffset);
                    TPKTile.TileInfo ti = new TPKTile.TileInfo(0, tileDataOffset);
                    tile = new TPKTile(zoomLevel, column, row, this.tpkFile.getImageFormat(), ti, bundleIndex);
                    tileData = ((TPKZoomLevelV1) tPKZoomLevel).getTileData(bundle, tile.tileInfo.tileDataOffset);
                    break;
                }
                case V2: {
                    long indexReadOffset
                            = 64
                            + ((bundleRow * 128) + (column - bundle.baseColumn))
                            * 8;

                    TPKTile.TileInfo ti = ((TPKZoomLevelV2) tPKZoomLevel).getTileInfo(bundle, indexReadOffset);
                    tile = new TPKTile(zoomLevel, column, row, this.tpkFile.getImageFormat(), ti, bundleIndex);
                    if (ti.tileLength > 0) {
                        tileData = bundle.bundleData.read(ti.tileDataOffset, ti.tileLength);
                    }
                    break;
                }

            }
            InputStream is = new ByteArrayInputStream(tileData);
            bi = ImageIO.read(is);
            if (bi != null) {
                return bi;
            }
        } catch (Exception e) {
        }

        return null;
    }

}
