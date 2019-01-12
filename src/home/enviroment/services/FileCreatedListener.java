package home.enviroment.services;

import java.nio.file.Path;


public interface FileCreatedListener {
	
	public void onFileCreated(Path file);
}
