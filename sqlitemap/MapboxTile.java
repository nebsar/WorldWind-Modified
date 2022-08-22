package gov.nasa.worldwind.layers.mercator.sqlitemap;

import gov.nasa.worldwind.layers.mercator.mbtiles.*;
import java.io.InputStream;

public class MapboxTile {
    private int zoom;
    private int column;
    private int row;
    private InputStream data;

    public MapboxTile(int zoom, int column, int row, InputStream tile_data) {
        this.zoom = zoom;
        this.column = column;
        this.row = row;
        this.data = tile_data;
    }

    public InputStream getData() {
        return data;
    }

    public int getZoom() {
        return zoom;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }
}