package com.maven.plugins.blog;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PathsTest {
	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { 
			{ "inputDir", "inputFile.md", "outputDir", "outputDir/inputFile.html" },
			{ "inputDir/dir", "inputFile", "outputDir", "outputDir/inputFile.html" },
			{ "inputDir/dir", "f/inputFile", "outputDir", "outputDir/f/inputFile.html" },
			{ "inputDir/dir/dir", "a/b/inputFile", "outputDir", "outputDir/a/b/inputFile.html" },
			{ "inputDir/dir/dir", "a/b/inputFile.txt", "outputDir/o", "outputDir/o/a/b/inputFile.html" },
			{ ".", "a/b/inputFile.md", "outputDir/o", "outputDir/o/a/b/inputFile.html" },
			{ ".", "./inputFile.md", "target", "target/inputFile.html" },
			{ ".", "inputFile.md", "target", "target/inputFile.html" },
			{ ".", "inputFile.md", "/home/carlos/md-html/target", "/home/carlos/md-html/target/inputFile.html" },
			{ ".", "inputFile.md", "t", "t/inputFile.html" },
		});
	}

	private final Path fInputDir;
	private final Path fInputFile;
	private final Path fOutputDir;
	private final Path fExpected;

	public PathsTest(String inputDir, String inputFile, String outputDir, String expected) {
		this.fInputDir = Paths.get(inputDir);
		this.fInputFile = Paths.get(inputFile);
		this.fOutputDir = Paths.get(outputDir);
		this.fExpected = Paths.get(expected);
	}
		
	@Test
	public void testChange() {
		Path inputFile = PathUtils.switchExtension(fInputFile, "html");
		Path observed = PathUtils.change(fInputDir, fInputDir.resolve(inputFile), fOutputDir);
		assertEquals(fExpected, observed);
	}
	
	@Test
	public void testExtNoPoint() {
		assertEquals(
				PathUtils.switchExtension(fInputFile, "html").getFileName().toString(), 
				"inputFile.html");
	}
	@Test
	public void testExtPoint() {
		assertEquals(
				PathUtils.switchExtension(fInputFile, ".html").getFileName().toString(), 
				"inputFile.html");
	}
}
