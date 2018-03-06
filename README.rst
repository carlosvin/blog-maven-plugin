Example how to create custom Maven Plugin
-----------------------------------------

Maven has lot of plugins to assist you in you project construction and deployment. For example if you want to compile C++ code instead of Java, you can use `native-maven-plugin <http://www.mojohaus.org/maven-native/native-maven-plugin/>`_ . But what if you need something more specific? Then you can create a custom Maven plugin. 

I will explain how to create a simple custom maven plugin to generate static blog site from Markdown files. I know we can already do that with `maven-site-pugin <https://maven.apache.org/plugins/maven-site-plugin/examples/creating-content.html>`_ since version 3.3, I will just use it for learning purposes.  

Project structure
=================

- src/main/java: Where Java source code is
- src/main/resources/META-INF/plexus/components.xml: file to create/override maven lifecycles and artifact types. Here we can especify which goals will be executed when for an artifact type, for example, we can say that for an artifact of type `whatever` when we run `mvn foo` it will verify the files, run tests, run linter, compile and zip al generated files.
- src/test/java: Unit tests folder
- src/it: Folder with all integration tests. Those integration tests are running actual projects and checking that ouputs are as expected.
- pom.xml: File to with Maven project description `(Project Object Model) <https://maven.apache.org/guides/introduction/introduction-to-the-pom.html>`_


Maven plugin concepts
=====================

`Mojo <http://maven.apache.org/plugin-developers/index.html>`_
An executable goal in Maven, e.g: `mvn your-plugin:your-mojo` will execute a maven goal `your-mojo` declared as part of `your-plugin`. 

Goal
It matches with `Mojo <http://maven.apache.org/plugin-developers/index.html>`_ execution

Lifecycle
It is a well defined sequence of phases. Each phase consists of a sequence of goals.
Let's see an example of lifecycle, e.g: FooLifecycle 

Work in progress...