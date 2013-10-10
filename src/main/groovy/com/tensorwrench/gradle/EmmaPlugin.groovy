/*
 Copyright 2013 TensorWrench, LLC
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.tensorwrench.gradle
import org.gradle.api.Plugin
import org.gradle.api.Project



class EmmaPlugin implements Plugin<Project> {
	def verbosityLevel = "info"
	def reportPath;
	def coverageFileName;
	def tmpDir;
	def instrDir;
	def metaDataFilePath;
	void apply(Project project) {
		reportPath 			= "${project.reporting.baseDir.absolutePath}/emma"
		coverageFileName	= "coverage"
		tmpDir				= "${project.buildDir}/tmp/emma"
		instrDir			= "${tmpDir}/instr"
		metaDataFilePath 	= "${tmpDir}/metadata.emma"
		project.configurations { emma }
		project.dependencies { emma "emma:emma:2.0.5312", "emma:emma_ant:2.0.5312" }
		project.tasks.test {
			// add EMMA related JVM args to our tests
			jvmArgs "-XX:-UseSplitVerifier", "-Demma.coverage.out.file=${project.buildDir}/tmp/emma/metadata.emma", "-Demma.coverage.out.merge=true"

			doFirst {
				logger.info "Instrumenting the classes at " + project.sourceSets.main.output.classesDir.absolutePath
				// define the custom EMMA ant tasks
				ant.taskdef( resource:"emma_ant.properties", classpath: project.configurations.emma.asPath)

				ant.path(id:"run.classpath") {
					pathelement(location:project.sourceSets.main.output.classesDir.absolutePath)
				}
				def emmaInstDir = new File(project.sourceSets.main.output.classesDir.parentFile.parentFile, "tmp/emma/instr")
				emmaInstDir.mkdirs()
				logger.debug "Creating $emmaInstDir to instrument from " +       project.sourceSets.main.output.classesDir.absolutePath
				// instruct our compiled classes and store them at ${project.buildDir}/tmp/emma/instr
				ant.emma(enabled: 'true', verbosity:'info'){
					instr(merge:"true", destdir: emmaInstDir.absolutePath, instrpathref:"run.classpath",
							metadatafile: new File(emmaInstDir, '/metadata.emma').absolutePath) {
								instrpath {
									fileset(dir:project.sourceSets.main.output.classesDir.absolutePath, includes:"**/*.class")
								}
							}
				}
				setClasspath(project.files("${project.buildDir}/tmp/emma/instr") + project.configurations.emma +    getClasspath())
			}

			// The report should be generated directly after the tests are done.
			// We create three types (txt, html, xml) of reports here. Running your build script now should
			// result in output like that:
			doLast {
				def srcDir = project.sourceSets.main.java.srcDirs.toArray()[0]
				logger.info "Creating test coverage reports for classes " + srcDir
				def emmaInstDir = new File(project.sourceSets.main.output.classesDir.parentFile.parentFile, "tmp/emma")
				ant.emma(enabled:"true"){
					new File("${project.buildDir}/reports/emma").mkdirs()
					report(sourcepath: srcDir){
						fileset(dir: emmaInstDir.absolutePath){ include(name:"**/*.emma") }
						txt(outfile:"${project.buildDir}/reports/emma/coverage.txt")
						html(outfile:"${project.buildDir}/reports/emma/coverage.html")
						xml(outfile:"${project.buildDir}/reports/emma/coverage.xml")
					}
				}
				logger.info "Test coverage reports available at ${project.buildDir}/reports/emma."
				logger.info "txt: ${project.buildDir}/reports/emma/coverage.txt"
				logger.info "Test ${project.buildDir}/reports/emma/coverage.html"
				logger.info "Test ${project.buildDir}/reports/emma/coverage.xml"
			}
		}

	}
}



//def emmaConvention = new EmmaPluginConvention(project)
//project.convention.plugins.emma = emmaConvention
//
//class EmmaPluginConvention{
//	def verbosityLevel = "info"
//	def reportPath;
//	def coverageFileName;
//	def tmpDir;
//	def instrDir;
//	def metaDataFilePath;
//
//	def emma(Closure close){
//		close.delegate = this;
//		close.run()
//	}
//
//	EmmaPluginConvention(Project project){
//
//	}
//}

