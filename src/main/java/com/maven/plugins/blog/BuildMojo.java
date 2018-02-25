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
	@Parameter(defaultValue = "${project.reporting.outputDirectory}", property = "siteOutputDirectory", required = true)
	private File outputDirectory;

	/**
	 * A specific <code>fileSet</code> rule to select files and directories.
	 */
	@Parameter
	private FileSet inputFiles;

	private final FileSetManager fileSetManager;

	private final ExecutorService executor;

	private final MdToHtml mdToHtml;
	private Path inputDirPath, outputDirPath;

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
		inputDirPath = Paths.get(inputFiles.getDirectory());
		outputDirPath = outputDirectory.toPath();
		String[] includedFiles = fileSetManager.getIncludedFiles(inputFiles);
		if (includedFiles == null || includedFiles.length == 0) {
			getLog().warn("SKIP: There are no input files. " + getInputFilesToString());
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
				getLog().debug("Processing results");
				executor.shutdown();
				processResult(results);
			} catch (InterruptedException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			}
		}
	}

	private void processResult(Map<Path, Future<Path>> results) throws InterruptedException {
		if (executor.awaitTermination(5, TimeUnit.MINUTES)) {
			getLog().info("Processed " + results.size() + " files");
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
		getLog().info("'inputFiles' is not configured, using defaults: " + getInputFilesToString());
	}

	private String getInputFilesToString() {
		return "Fileset matching " + inputFiles.getIncludes() + " in " + inputFiles.getDirectory();
	}

	class ConvertToHtmlTask implements Callable<Path> {

		private final Path input;

		public ConvertToHtmlTask(Path input) {
			this.input = input;
		}

		public Path call() throws Exception {
			Path out = getOutputPath();
			getLog().info("Processing " + input + " > " + out);
			String html = mdToHtml.getHtml(input);
			Files.write(out, html.getBytes(), StandardOpenOption.CREATE);
			return out;
		}

		public Path getOutputPath() {
			return PathUtils.change(
					inputDirPath, 
					PathUtils.switchExtension(input, ".html"), 
					outputDirPath);
		}
	}
}
