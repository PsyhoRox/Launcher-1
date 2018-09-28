package ru.gravit.utils.cacher;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

public class CachedDir {
	public class CachingVisitor {
		
	}
	
	public CachedDir(Path cacheDir, Path dir) {
		
	}
	
	private CachedDir(File dir, File cacheDir) {
		
	}
	
	public CachedDir(File dir, File cacheDir, URL url) {
		this(dir, cacheDir);
	}
}