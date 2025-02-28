// Copyright 2015 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtools.build.lib.analysis;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.devtools.build.lib.actions.Artifact;
import com.google.devtools.build.lib.actions.RunfilesSupplier;
import com.google.devtools.build.lib.actions.RunfilesSupplier.RunfilesTree;
import com.google.devtools.build.lib.analysis.config.BuildConfigurationValue.RunfileSymlinksMode;
import com.google.devtools.build.lib.collect.nestedset.NestedSet;
import com.google.devtools.build.lib.skyframe.serialization.autocodec.AutoCodec;
import com.google.devtools.build.lib.vfs.PathFragment;
import java.util.Map;
import javax.annotation.Nullable;

/** {@link RunfilesSupplier} implementation wrapping a single {@link Runfiles} directory mapping. */
@AutoCodec
public final class SingleRunfilesSupplier implements RunfilesSupplier, RunfilesTree {

  private final PathFragment runfilesDir;
  private final Runfiles runfiles;
  @Nullable private final Artifact repoMappingManifest;
  private final RunfileSymlinksMode runfileSymlinksMode;
  private final boolean buildRunfileLinks;

  /**
   * Create an instance mapping {@code runfiles} to {@code runfilesDir}.
   *
   * @param runfilesDir the desired runfiles directory. Should be relative.
   * @param runfiles the runfiles for runilesDir.
   * @param runfileSymlinksMode how to create runfile symlinks
   * @param buildRunfileLinks whether runfile symlinks should be created during the build
   */
  @AutoCodec.Instantiator
  public SingleRunfilesSupplier(
      PathFragment runfilesDir,
      Runfiles runfiles,
      @Nullable Artifact repoMappingManifest,
      RunfileSymlinksMode runfileSymlinksMode,
      boolean buildRunfileLinks) {
    checkArgument(!runfilesDir.isAbsolute());
    this.runfilesDir = checkNotNull(runfilesDir);
    this.runfiles = checkNotNull(runfiles);
    this.repoMappingManifest = repoMappingManifest;
    this.runfileSymlinksMode = runfileSymlinksMode;
    this.buildRunfileLinks = buildRunfileLinks;
  }

  @Override
  public ImmutableList<RunfilesTree> getRunfilesTrees() {
    return ImmutableList.of(this);
  }

  @Override
  public NestedSet<Artifact> getArtifacts() {
    return runfiles.getAllArtifacts();
  }

  @Override
  public PathFragment getPossiblyIncorrectExecPath() {
    return runfilesDir;
  }

  @Override
  public Map<PathFragment, Artifact> getMapping() {
    return runfiles.getRunfilesInputs(
        /* eventHandler= */ null, /* location= */ null, repoMappingManifest);
  }

  @Override
  public RunfileSymlinksMode getSymlinksMode() {
    return runfileSymlinksMode;
  }

  @Override
  public boolean isBuildRunfileLinks() {
    return buildRunfileLinks;
  }

  @Override
  public String getWorkspaceName() {
    return runfiles.getSuffix().getPathString();
  }
}
