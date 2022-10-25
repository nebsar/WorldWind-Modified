package trial;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.data.DataStoreProducer;
import gov.nasa.worldwind.data.TiledImageProducer;
import gov.nasa.worldwind.util.WWIO;
import java.io.File;
import org.w3c.dom.Document;

/**
 *
 * @author n.sarikaya
 */
public class FileConverter {

    public static void main(String[] args) {
        // Given a source image, and a path to a folder in the local file system which receives the image tiles, configure a
        // TiledImageProducer to create a pyramid of images tiles in the World Wind Java cache format.
        String imagePath = "C:\\Users\\n.sarikaya\\OneDrive - SimFront\\Desktop\\1_250,000\\JAAF3301.IMG";
        String tiledImagePath = "C:/Development/TRIAL";
        String tiledImageDisplayName = "Trial";

        // Create a parameter list which defines where the image is imported, and the name associated with it.
        AVList params = new AVListImpl();
        params.setValue(AVKey.FILE_STORE_LOCATION, WWIO.getParentFilePath(tiledImagePath));
        params.setValue(AVKey.DATA_CACHE_NAME, WWIO.getFilename(tiledImagePath));
        params.setValue(AVKey.DATASET_NAME, tiledImageDisplayName);

        // Create a TiledImageProducer to transform the source image to a pyramid of images tiles in the World Wind
        // Java cache format.
        DataStoreProducer producer = new TiledImageProducer();
        try {
            // Configure the TiledImageProducer with the parameter list and the image source.
            producer.setStoreParameters(params);
            producer.offerDataSource(new File(imagePath), null);
            // Import the source image into the FileStore by converting it to the World Wind Java cache format. This throws
            // an exception if production fails for any reason.
            producer.startProduction();
        } catch (Exception e) {
            // Exception attempting to create the image tiles. Revert any change made during production.
            producer.removeProductionState();
        }

        // Extract the data configuration document from the production results. If production sucessfully completed, the
        // TiledImageProducer's production results contain a Document describing the converted imagery as a data
        // configuration document.
        Iterable results = producer.getProductionResults();
        if (results == null || results.iterator() == null || !results.iterator().hasNext()) {
            return;
        }

        Object o = results.iterator().next();
        if (o == null || !(o instanceof Document)) {
            return;
        }

        Document dataConfigDoc = (Document) o;
    }
}
