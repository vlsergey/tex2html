package com.github.vlsergey.tex2html;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@SpringBootApplication
@EnableConfigurationProperties
public class Tex2Html implements CommandLineRunner, ExitCodeGenerator {

	public static void main(String[] args) {
		System.exit(SpringApplication.exit(SpringApplication.run(Tex2Html.class, args)));
	}

	private int exitCode;

	@Autowired
	private IFactory factory;

	@Autowired
	private Tex2HtmlCommand tex2HtmlCommand;

	@Override
	public int getExitCode() {
		return exitCode;
	}

	@Override
	public void run(String... args) {
		// let picocli parse command line args and run the business logic
		exitCode = new CommandLine(tex2HtmlCommand, factory).execute(args);
	}
}
