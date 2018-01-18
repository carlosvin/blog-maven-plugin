package com.maven.plugins.blog;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

/**
 * Build the project
 */
@Mojo(name = "build", defaultPhase = LifecyclePhase.COMPILE)
public class BuildMojo extends AbstractMojo {
	/**
	 * Location of the file.
	 */
	@Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
	private File outputDirectory;
	
	/**
     * A specific <code>fileSet</code> rule to select files and directories.
     */
	@Parameter
	private FileSet inputFiles;

	private final FileSetManager fileSetManager;

	private final ExecutorService executor;

	private final MdToHtml mdToHtml;

	@Inject
	public BuildMojo(FileSetManager fileSetManager, MdToHtml mdToHtml) {
		this.fileSetManager = fileSetManager;
		this.executor = Executors.newCachedThreadPool();
		this.mdToHtml = mdToHtml;
	}

	public void execute() throws MojoExecutionException {
		if (inputFiles == null) {
			setDefaultInput();
		}
		String[] includedFiles = fileSetManager.getIncludedFiles(inputFiles);
		if (includedFiles == null || includedFiles.length == 0) {
			getLog().warn("SKIP: There are no input files matching " + inputFiles);
		} else {
			if (!outputDirectory.exists()) {
				outputDirectory.mkdirs();
			}
			ArrayList<Callable<Path>> tasks = new ArrayList<Callable<Path>>();
			for (String f: includedFiles) {
				tasks.add(new ConvertToHtmlTask(Paths.get(f)));
			}
			try {
				List<Future<Path>> futures = executor.invokeAll(tasks);
				if (executor.awaitTermination(10, TimeUnit.MINUTES)) {
					getLog().info("Done");
				} else {
					getLog().error("Timeout");
				}
				// TODO process futures
			} catch (InterruptedException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			}
		}
	}

	private void setDefaultInput() {
		this.inputFiles = new FileSet();
		this.inputFiles.addInclude("**/*.md");
		this.inputFiles.addInclude("**/*.MD");
		this.inputFiles.setDirectory(".");
		getLog().info("'inputFiles' is not configured, using defaults: " + this.inputFiles);
	}
	
	class ConvertToHtmlTask implements Callable<Path> {
		
		private final Path input;

		public ConvertToHtmlTask(Path input) {
			this.input = input;
		}

		public Path call() throws Exception {
			String html = mdToHtml.getHtml(this.input);
			// TODO Auto-generated method stub
			return input;
		}
		
		
		
	}
}
