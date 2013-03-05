package syncronize.files;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * This Maven Plugin can be used to synchronize message bundle files using a
 * file as starting point
 * 
 * We all forget to go thru all the languages and insert the newly key created
 * in the main bundle file
 * 
 * @author mcristea
 * 
 */
@Mojo(name = "process", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class SyncronizeThem extends AbstractMojo {

	/**
	 * This parameter defines the target files pattern that will be processed
	 * *.properties
	 * see {@link WildcardFileFilter} for more details
	 */
	@Parameter(property = "pattern", required = true)
	private String targetPattern;
	
	/**
	 * This parameter defines the target directory that contains the message bundle files
	 * use absolute path for it e.g. d:/work/project/war/src/main/resources/properties/.
	 */
	@Parameter(property = "targetDirPath", required = true)
	private String targetDirPath;
	
	/**
	 * This parameter defines the base file from where all the keys will be
	 * pulled
	 */
	@Parameter(property = "baseFile", required = true)
	private String baseFileName;

	public void execute() throws MojoExecutionException {
		getLog().info( "SyncronizeThem execution started");
		if(StringUtils.isEmpty(baseFileName) || StringUtils.isEmpty(targetDirPath) || StringUtils.isEmpty(targetPattern) ){
			getLog().info("Target Path dir is --->" + "should not be empty!");
		}
		
		try {
			Configuration baseConfiguration = new PropertiesConfiguration(baseFileName);
			PropertiesConfiguration targetConfiguration = null;
			String key = null;
			Iterator<String> baseIterator = null;
			
			File dir = new File(targetDirPath);
			getLog().info("Target Path dir is --->" + dir.getAbsolutePath());
			
			getLog().info("Target files patern is --->" + targetPattern);
			FileFilter fileFilter = new WildcardFileFilter(targetPattern);
			
			File[] files = dir.listFiles(fileFilter);
			getLog().info( "Found "+files.length + " matching the pattern.");
			
			for (int i = 0; i < files.length; i++) {
				targetConfiguration = new PropertiesConfiguration(files[i]);
				baseIterator = baseConfiguration.getKeys();
				while(baseIterator.hasNext()){
					key = baseIterator.next();
					if(!targetConfiguration.containsKey(key)){
						targetConfiguration.addProperty(key, baseConfiguration.getProperty(key));
						getLog().info( files[i].getName() +"-> added key: " + key + " with value: " + baseConfiguration.getProperty(key));
					}
				}
				targetConfiguration.save();
				getLog().info( "Saved changes in file: "+ targetConfiguration.getFileName());
			}
		} catch (ConfigurationException e) {
			throw new MojoExecutionException("An exception occured when building the base configuration file: ", e);
		}
	}

	public void setTargetPattern(String targetPattern) {
		this.targetPattern = targetPattern;
	}

	public void setTargetDirPath(String targetDirPath) {
		this.targetDirPath = targetDirPath;
	}

	public void setBaseFile(String baseFileName) {
		this.baseFileName = baseFileName;
	}
}
