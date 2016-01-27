package com.e_gineering.maven.gitflowhelper;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * When executed, attaches artifacts from a previously deployed (to a repository) build of this
 * project to the current build execution.
 */
@Mojo(name = "attach-deployed", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true)
@Execute(phase = LifecyclePhase.CLEAN)
public class AttachDeployedArtifactsMojo extends AbstractGitflowBasedRepositoryMojo {
    @Override
    protected void execute(GitBranchType type, String gitBranch, String branchPattern) throws MojoExecutionException, MojoFailureException {
        switch (type) {
            case MASTER: {
                getLog().info("Attaching artifacts from release repository...");
                attachExistingArtifacts(releaseDeploymentRepository, true);
                break;
            }
            case RELEASE:
            case HOTFIX:
            case BUGFIX: {
                getLog().info("Attaching artifacts from stage repository...");
                attachExistingArtifacts(stageDeploymentRepository, true);
                break;
            }
            case DEVELOPMENT: {
                getLog().info("Attaching artifacts from snapshot repository...");
                attachExistingArtifacts(snapshotDeploymentRepository, true);
                break;
            }
            default: {
                getLog().info("Attaching Artifacts from local repository...");
                // Use the 'local' repository to do this.
                attachExistingArtifacts(null, false);
            }
        }

        File targetDir = new File(project.getBuild().getDirectory());
        for (Artifact artifact : project.getAttachedArtifacts()) {
            try {
                FileUtils.copyFileToDirectory(artifact.getFile(), targetDir);
            } catch (IOException ioe) {
                throw new MojoExecutionException("Failed to copy attached artifact to output directory.", ioe);
            }
        }

        try {
            FileUtils.copyFile(project.getArtifact().getFile(), new File(targetDir, project.getBuild().getFinalName() + "." + FileUtils.getExtension(project.getArtifact().getFile().getName())));
        } catch (IOException ioe) {
            throw new MojoExecutionException("Failed to copy project artifact to: " + project.getBuild().getFinalName() + "." + FileUtils.getExtension(project.getArtifact().getFile().getName()), ioe);
        }
    }
}