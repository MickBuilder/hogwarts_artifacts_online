package fr.mikeb.learning.hogwarts_artifacts_online.system.exception;

public class NotFoundException extends RuntimeException{
  public NotFoundException(String objectName, String id) {
    super("Could not find " + objectName + " with Id " + id +" :(");
  }
}
