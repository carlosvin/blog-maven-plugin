package com.maven.plugins.blog;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
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
			Map<Path, Future<Path>> results = new HashMap<>();
			for (String f : includedFiles) {
				final Path path = Paths.get(f);
				results.put(path, executor.submit(new ConvertToHtmlTask(path)));
			}
			try {
				processResult(results);
				// TODO process futures
			} catch (InterruptedException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			}
		}
	}

	private void processResult(Map<Path, Future<Path>> results) throws InterruptedException {
		if (executor.awaitTermination(10, TimeUnit.MINUTES)) {
			getLog().info("Done");
		} else {
			getLog().error("Timeout processing files");
		}
		for (Entry<Path, Future<Path>> entry : results.entrySet()) {
			try {
				Path outputFile = entry.getValue().get();
				getLog().debug(entry.getKey() + " > " + outputFile);
			} catch (ExecutionException e) {
				getLog().error("Error converting " + entry.getKey(), e);
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
			String html = mdToHtml.getHtml(input);
			Path out = getOutputPath();
			Files.write(getOutputPath(), html.getBytes(), StandardOpenOption.CREATE);
			return out;
		}

		public Path getOutputPath() {
			String fileName = input.getFileName().toString();
			int i = fileName.lastIndexOf('.');
			Path newFile = input.resolveSibling(fileName.substring(0, i) + ".html");
			Path relNewFile = Paths.get(inputFiles.getDirectory()).relativize(newFile);
			return outputDirectory.toPath().resolve(relNewFile);
		}

	}
}
