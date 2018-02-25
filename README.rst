Example how to create custom Maven Plugin
-----------------------------------------

Maven has lot of plugins to assist you in you project construction and deployment. For example if you want to compile C++ code instead of Java, you can use `native-maven-plugin <http://www.mojohaus.org/maven-native/native-maven-plugin/>`_ . But what if you need something more specific? Then you can create a custom Maven plugin. 

I will explain how to create a simple custom maven plugin to generate static blog site from Markdown files. I know we can already do that with `maven-site-pugin <https://maven.apache.org/plugins/maven-site-plugin/examples/creating-content.html>`_ since version 3.3, I will just use it for learning purposes.  

Project structure (under src folder)
====================================

- main/java: Where java source folders are
- main/resources/META-INF/plexus/components.xml: file to create/override maven lifecycles and artifact types. Here we can especify which goals will be executed when for an artifact type, for example, we can say that for an artifact of type `whatever` when we run `mvn foo` it will verify the files, run tests, run linter, compile and zip al generated files.

Work in progress...