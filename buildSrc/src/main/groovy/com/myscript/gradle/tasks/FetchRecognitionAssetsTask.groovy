/*
 * Copyright (c) MyScript. All rights reserved.
 */

package com.myscript.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Download resource assets from remote server.
 */
@SuppressWarnings("GroovyUnusedDeclaration")
class FetchRecognitionAssetsTask extends DefaultTask {

    @TaskAction
    void fetchRecognitionAssets() {
        // Thanks to https://gitreleases.dev/
        def baseUrl = "https://gitreleases.dev/gh/jingkecn/myscript-iink-recognition-assets/latest"
        def urls = [
                "$baseUrl/myscript-iink-recognition-diagram.zip",
                "$baseUrl/myscript-iink-recognition-raw-content.zip",
                "$baseUrl/myscript-iink-recognition-math.zip",
                "$baseUrl/myscript-iink-recognition-text-en_US.zip"
        ]

        def intoDir = project.file("$project.projectDir/src/main/assets")
        if (!intoDir.isDirectory())
            intoDir.mkdirs()

        def diagramConf = project.file("$intoDir/conf/diagram.conf")
        def rawContentConf = project.file("$intoDir/conf/raw-content.conf")
        def mathConf = project.file("$intoDir/conf/math.conf")
        def enUSConf = project.file("$intoDir/conf/en_US.conf")

        if (!diagramConf.exists() || !rawContentConf.exists() || !mathConf.exists()
                || !enUSConf.exists()) {
            def fromDir = project.file("$intoDir/temp")
            if (!fromDir.isDirectory())
                fromDir.mkdirs()

            // download resource zips
            urls.each { url ->
                ant.get(src: url, dest: fromDir.getPath())
            }

            // unzip
            fromDir.listFiles({ it.name.endsWith(".zip") } as FileFilter).each {
                def filePath = it.getPath()
                project.copy {
                    from project.zipTree(filePath)
                    into fromDir
                }
            }

            // copy into assets folder
            project.copy {
                from "$fromDir/recognition-assets"
                into intoDir
            }

            // delete useless files
            project.delete(fromDir)
        }
    }
}
