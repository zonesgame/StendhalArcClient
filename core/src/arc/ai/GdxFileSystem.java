/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package arc.ai;

import java.io.File;

import arc.Core;
import arc.Files.FileType;
import arc.assets.loaders.FileHandleResolver;
import arc.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import arc.assets.loaders.resolvers.ClasspathFileHandleResolver;
import arc.assets.loaders.resolvers.ExternalFileHandleResolver;
import arc.assets.loaders.resolvers.InternalFileHandleResolver;
import arc.assets.loaders.resolvers.LocalFileHandleResolver;
import arc.files.Fi;

/** @author davebaol */
public class GdxFileSystem implements FileSystem {

	public GdxFileSystem () {
	}

	@Override
	public FileHandleResolver newResolver (FileType fileType) {
		switch (fileType) {
		case absolute:
			return new AbsoluteFileHandleResolver();
		case classpath:
			return new ClasspathFileHandleResolver();
		case external:
			return new ExternalFileHandleResolver();
		case internal:
			return new InternalFileHandleResolver();
		case local:
			return new LocalFileHandleResolver();
		}
		return null; // Should never happen
	}

	@Override
	public Fi newFileHandle (String fileName) {
		return Core.files.absolute(fileName);
	}

	@Override
	public Fi newFileHandle (File file) {
		return Core.files.absolute(file.getAbsolutePath());
	}

	@Override
	public Fi newFileHandle (String fileName, FileType type) {
		return Core.files.get(fileName, type);
	}

	@Override
	public Fi newFileHandle (File file, FileType type) {
		return Core.files.get(file.getPath(), type);
	}

}
