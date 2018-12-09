package home.enviroment.modulemanager.test.jobs;

import static org.junit.Assert.*;
import home.enviroment.job.PersistSenseMesurementJob;
import home.enviroment.modulemanager.test.utils.MesurementDataProvider;
import home.enviroment.sense.SenseMesurement;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.BeforeClass;
import org.junit.Test;

public class PersistSenseMesurementJobTest {
	
	private static final String OUTPUT_FOLDER = "persist/output/";

	private static MesurementDataProvider mesurementsDataProvider;
	private static List<SenseMesurement> mesurements;
	private static List<String> inputLines;
	private static List<String> outputLines;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("logging.configuration", "conf/test/logging.properties");
		System.setProperty("application.configuration", "conf/test/enviroment-monitor.properties");
		mesurementsDataProvider = MesurementDataProvider.getInstance();

		inputLines = mesurementsDataProvider.getInputLines();
		mesurements = mesurementsDataProvider.getMesurements();
	}

	@Test
	public void test() throws InterruptedException, IOException, ExecutionException {
		PersistSenseMesurementJob job = new PersistSenseMesurementJob(mesurements, OUTPUT_FOLDER);
		ExecutorService exec = Executors.newSingleThreadExecutor();
		Future<Path> res = exec.submit(job);
		Path fileCreated = res.get();
		assertNotNull(fileCreated);
		outputLines = Files.readAllLines(fileCreated);
		assertTrue(outputLines.containsAll(inputLines));
		assertTrue(inputLines.containsAll(outputLines));
		int sizeBeforeIntersection = outputLines.size();
		outputLines.retainAll(inputLines); 
		int sizeAfterIntersection = outputLines.size();
		assertEquals(sizeBeforeIntersection, sizeAfterIntersection);
		
		//Delete old files in output directory other then the last one created
		DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(OUTPUT_FOLDER));
		Iterator<Path> filesIter = dirStream.iterator();
		while(filesIter.hasNext()) {
			Path file = filesIter.next();
			if(!file.equals(fileCreated) && !Files.isDirectory(file)) {
				Files.delete(file);
			}
		}
	}
}
