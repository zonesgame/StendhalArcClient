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

import arc.Files.FileType;
import arc.assets.loaders.FileHandleResolver;
import arc.files.Fi;
import arc.util.ArcRuntimeException;

/** @author davebaol */
public class StandaloneFileSystem implements FileSystem {

	public StandaloneFileSystem () {
	}

	@Override
	public FileHandleResolver newResolver (final FileType fileType) {
		return new FileHandleResolver() {

			@Override
			public Fi resolve (String fileName) {
				return new DesktopFileHandle(fileName, fileType);
			}
		};
	}

	@Override
	public Fi newFileHandle (String fileName) {
		return new DesktopFileHandle(fileName, FileType.absolute);
	}

	@Override
	public Fi newFileHandle (File file) {
		return new DesktopFileHandle(file, FileType.absolute);
	}

	@Override
	public Fi newFileHandle (String fileName, FileType type) {
		return new DesktopFileHandle(fileName, type);
	}

	@Override
	public Fi newFileHandle (File file, FileType type) {
		return new DesktopFileHandle(file, type);
	}

	public static class DesktopFileHandle extends Fi {

		static public final String externalPath = System.getProperty("user.home") + File.separator;
		static public final String localPath = new File("").getAbsolutePath() + File.separator;

		public DesktopFileHandle (String fileName, FileType type) {
			super(fileName, type);
		}

		public DesktopFileHandle (File file, FileType type) {
			super(file, type);
		}

		public Fi child (String name) {
			if (file.getPath().length() == 0) return new DesktopFileHandle(new File(name), type);
			return new DesktopFileHandle(new File(file, name), type);
		}

		public Fi sibling (String name) {
			if (file.getPath().length() == 0) throw new ArcRuntimeException("Cannot get the sibling of the root.");
			return new DesktopFileHandle(new File(file.getParent(), name), type);
		}

		public Fi parent () {
			File parent = file.getParentFile();
			if (parent == null) {
				if (type == FileType.absolute)
					parent = new File("/");
				else
					parent = new File("");
			}
			return new DesktopFileHandle(parent, type);
		}

		public File file () {
			if (type == FileType.external) return new File(DesktopFileHandle.externalPath, file.getPath());
			if (type == FileType.local) return new File(DesktopFileHandle.localPath, file.getPath());
			return file;
		}
	}
}
