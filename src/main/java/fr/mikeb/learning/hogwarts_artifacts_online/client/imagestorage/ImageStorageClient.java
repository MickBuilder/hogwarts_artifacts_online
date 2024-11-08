package fr.mikeb.learning.hogwarts_artifacts_online.client.imagestorage;

import java.io.IOException;
import java.io.InputStream;

public interface ImageStorageClient {
  String uploadImage(String containerName, String originalImageName, InputStream data, long length) throws IOException;
}
