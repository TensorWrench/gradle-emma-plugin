gradle-emma-plugin
==================

Adds emma coverage to Java unit tests.  The plugin automatically the "main" and "test" source as part of the "test" 
task and generates a report in ${buildDir}/reports/emma. 


Usage
=====

Add to your build.gradle
	buildscript {
		repositories {
			mavenCentral()
		}
		dependencies {
			classpath "com.tensorwrench.gradle:gradle-emma-plugin:+"
		}
	}
	apply plugin: 'emma'
