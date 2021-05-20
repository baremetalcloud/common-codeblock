version="0.1"
group="com.baremetalcloud"

plugins {
  kotlin("multiplatform") version("1.5.0") 
  id("com.vanniktech.maven.publish") version("0.15.1") 
}

repositories {
  mavenCentral()
}


kotlin {
  jvm {
    compilations.all {
      kotlinOptions.freeCompilerArgs = listOf("-Xmulti-platform")
      kotlinOptions.allWarningsAsErrors
    }
    testRuns["test"].executionTask.configure {
      useJUnit()
    }
  }
  js(LEGACY) {
    nodejs()
    useCommonJs()
  }
  val hostOs = System.getProperty("os.name")
      val isMingwX64 = hostOs.startsWith("Windows")        
      val nativeTarget = when {
          hostOs == "Mac OS X" -> macosX64("native")
          hostOs == "Linux" -> linuxX64("native")
          isMingwX64 -> mingwX64("native")
          else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
      }        
  explicitApiWarning()
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(kotlin("stdlib-common"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation(kotlin("stdlib-jdk8"))
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(kotlin("test-junit"))
      }
    }
    val jsMain by getting {
      dependencies {
        implementation(kotlin("stdlib-js"))
      }
    }
    val jsTest by getting {
      dependencies {
        implementation(kotlin("test-js"))
      }
    }
    val nativeMain by getting {
      dependencies {
      }
    }
    val nativeTest by getting {
      dependencies {
      }
    }
  }
}


plugins.withId("com.vanniktech.maven.publish") {
  mavenPublish {
    sonatypeHost = com.vanniktech.maven.publish.SonatypeHost.S01
  }
}




