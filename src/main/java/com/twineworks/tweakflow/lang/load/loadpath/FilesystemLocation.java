/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Twineworks GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.twineworks.tweakflow.lang.load.loadpath;

import com.twineworks.tweakflow.lang.errors.LangError;
import com.twineworks.tweakflow.lang.errors.LangException;
import com.twineworks.tweakflow.lang.load.user.UserObjectFactory;
import com.twineworks.tweakflow.lang.parse.units.FilesystemParseUnit;
import com.twineworks.tweakflow.lang.parse.units.ParseUnit;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class FilesystemLocation implements LoadPathLocation {

  private final Path rootPath;
  private final Path absRootPath;
  private final boolean strict;
  private final UserObjectFactory userObjectFactory = new UserObjectFactory();
  private final String defaultExtension;


  public FilesystemLocation(Path rootPath, boolean strict, String defaultExtension){
    Objects.requireNonNull(rootPath, defaultExtension);
    this.rootPath = rootPath;
    this.absRootPath = rootPath.toAbsolutePath();
    this.strict = strict;
    this.defaultExtension = defaultExtension;
  }

  public FilesystemLocation(Path rootPath) {
    this(rootPath, true, ".tf");
  }

  public FilesystemLocation(Path rootPath, boolean strict) {
    this(rootPath, strict, ".tf");
  }

  public Path getRootPath() {
    return rootPath;
  }

  @Override
  public boolean pathExists(String path) {

    path = resolve(path);
    return pathAccessible(path) && Paths.get(path).toFile().exists();

  }

  private boolean pathAccessible(String path){
    return !strict || path.startsWith(pathToString(absRootPath));
  }

  @Override
  public ParseUnit getParseUnit(String path) {

    path = resolve(path);
    if (!pathAccessible(path)){
      throw new LangException(LangError.CANNOT_FIND_MODULE, "cannot access path "+path+" from file system location "+absRootPath);
    }
    return new FilesystemParseUnit(this, resolve(path));
  }

  @Override
  public String resolve(String path) {

    path = applyDefaultExtension(path);

    return pathToString(rootPath.resolve(path).toAbsolutePath().normalize());
  }

  private String applyDefaultExtension(String path) {
    if (!path.endsWith(defaultExtension)){
      path = path + defaultExtension;
    }
    return path;
  }

  private String pathToString(Path path){
    return path.toString().replace('\\', '/');
  }

  @Override
  public UserObjectFactory getUserObjectFactory() {
    return userObjectFactory;
  }

}
