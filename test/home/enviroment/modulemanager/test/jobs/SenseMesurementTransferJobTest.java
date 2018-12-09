package home.enviroment.modulemanager.test.jobs;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import home.enviroment.config.Prop;
import home.enviroment.job.SenseMesurementTransferJob;
import home.enviroment.modulemanager.test.utils.ServiceManager;
import home.enviroment.services.ConfigurationService;
import home.enviroment.services.FileTransferedListener;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.util.concurrent.Service;

public class SenseMesurementTransferJobTest {
	
	private static ConfigurationService configurationService;
	private static final String OUTPUT_FOLDER = "persist/output/";
	private static final Collection<Path> transferedFiles = new LinkedList<Path>();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("logging.configuration", "conf/test/logging.properties");
		System.setProperty("application.configuration", "conf/test/enviroment-monitor.properties");
		configurationService = ConfigurationService.getInstance();
		ServiceManager.startService(configurationService);
	}

	@Test
	public void test() throws IOException, InterruptedException {
		SenseMesurementTransferJob job = new SenseMesurementTransferJob(getFilesToTransfer(), getRemoteProps());
		job.addFileTransferedListener(new FileTransferedListener() {
			
			@Override
			public void onFileTransfered(String file) {
				transferedFiles.add(Paths.get(file));
			}
		});
		Thread transferThread = new Thread(job);
		transferThread.start();
		transferThread.join();
		assertTrue(allFilesRemovedRemoteFolder());
		assertTrue(allFilesExistInRemoteFolder());
	}

	private static boolean allFilesExistInRemoteFolder() {
		boolean res = true;
		String remoteFolder = configurationService.getProperty(Prop.REMOTE_TRANSFER_FOLDER);
		for(Path file : transferedFiles) {
			Path remoteFile = Paths.get(remoteFolder + file.getFileName());
			if(!Files.exists(remoteFile)) {
				return false;
			}
		}
		return res;		
	}
	
	private static boolean allFilesRemovedRemoteFolder() {
		boolean res = true;
		System.out.println("Files: " + transferedFiles);
		for(Path file : transferedFiles) {
			System.out.println(file);
			if(Files.exists(file)) {
				return false;
			}
		}
		return res;
	}
	
	private List<Path> getFilesToTransfer() throws IOException {
		List<Path> files = new ArrayList<>();
		DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
			
			@Override
			public boolean accept(Path entry) throws IOException {
				return Files.isRegularFile(entry);
			}
		};

		DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(OUTPUT_FOLDER), filter);
		Iterator<Path> iter = directoryStream.iterator();
		while(iter.hasNext()) {
			files.add(iter.next());
		}
		return files;
	}

	private Map<String, String> getRemoteProps() {
		Map<String, String> remoteProps = new HashMap<>();
		remoteProps.put(SenseMesurementTransferJob.REMOTE_USER_PROP, configurationService.getProperty(Prop.REMOTE_TRANSFER_USER));
		remoteProps.put(SenseMesurementTransferJob.REMOTE_PASS_PROP, configurationService.getProperty(Prop.REMOTE_TRANSFER_PASS));
		remoteProps.put(SenseMesurementTransferJob.REMOTE_HOST_PROP, configurationService.getProperty(Prop.REMOTE_TRANSFER_HOST));
		remoteProps.put(SenseMesurementTransferJob.REMOTE_PORT_PROP, configurationService.getProperty(Prop.REMOTE_TRANSFER_PORT));
		remoteProps.put(SenseMesurementTransferJob.CONNECT_TO_PROP, configurationService.getProperty(Prop.REMOTE_TRANSFER_CONNECT_TO));
		remoteProps.put(SenseMesurementTransferJob.REMOTE_FOLDER_PROP, configurationService.getProperty(Prop.REMOTE_TRANSFER_FOLDER));
		return remoteProps;
	}

	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ServiceManager.stopService(configurationService);
	}
}
