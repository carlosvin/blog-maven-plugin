package com.maven.plugins.blog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.inject.Named;
import javax.inject.Singleton;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

@Named
@Singleton
public class MdToHtml {

	private final Parser parser;
	private final HtmlRenderer renderer;

	public MdToHtml() {
		parser = Parser.builder().build();
		renderer = HtmlRenderer.builder().build();
	}
	
	public String getHtml(String md){
		Node document = parser.parse(md);
		return renderer.render(document);	
	}
	
	public String getHtml(Path mdFile) throws IOException {
		Node document = parser.parseReader(Files.newBufferedReader(mdFile));
		return renderer.render(document);	
	}
	
}
