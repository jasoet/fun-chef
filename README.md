# Ktor Client Chef 

[![Build Status](https://travis-ci.org/jasoet/ktor-client-chef.svg?branch=master)](https://travis-ci.org/jasoet/ktor-client-chef)
[![JCenter](https://api.bintray.com/packages/jasoet/ktor/ktor-client-chef/images/download.svg)](https://bintray.com/jasoet/ktor/ktor-client-chef/_latestVersion)
[![Maven Central](https://img.shields.io/maven-central/v/id.jasoet/ktor-client-chef.svg)](http://search.maven.org/#artifactdetails%7Cid.jasoet%7Cfktor-client-chef%7C1.0.0%7Cjar)

[Ktor Client] [Feature] to enable authentication mechanism to [Chef Server API].

Accessing Chef Server API [require](https://docs.chef.io/api_chef_server.html#requirements) detailed and complicated auth mechanism. 
This Kotlin library wrap Chef Server API authentication as Ktor Client Feature.

## Gradle
### Add Maven Central or JCenter repository
```groovy
repositories {
    jcenter()
}
```

```groovy
repositories {
    mavenCentral()
}
```

### Add dependency 
```groovy
compile 'id.jasoet:ktor-client-chef:<version>'
```

## Maven
### Add dependency
```xml
<dependency>
  <groupId>id.jasoet</groupId>
  <artifactId>ktor-client-chef</artifactId>
  <version>VERSION</version>
  <type>pom</type>
</dependency>
```

## Usage
### Simple Usage
This library provide `ChefApiClient` object with preconfigured Ktor Client. You can use it directly as Chef Api Client.
Requires some parameter, parameter can be supplied from `System Properties`, file or environment variable and handled by [Typesafe Config].

| Name | Description | Type | Default | Required |
|------|-------------|:----:|:-----:|:-----:|
| USER_ID | Chef user id | string | - | yes |
| USER_PEM_LOCATION | Chef user private key location | string | - | yes |
| CHEF_SERVER_HOST | Chef server host | string | - | yes |
| CHEF_ORGANIZATION | Chef organization | string | - | no |
| CHEF_VERSION | Chef server version | string | 12.22.5 | no |

If you set `CHEF_ORGANIZATION` url for request will prefixed by `/organizations/<NAME>/` so you don't need to include that on your request. 


#### Example
```kotlin
// Set config
System.setProperty("USER_ID", "chef-client")
System.setProperty("USER_PEM_LOCATION", "/home/chef-client/.chef/client.pem")
System.setProperty("CHEF_SERVER_HOST", "https://chef-server.jasoet.id")
System.setProperty("CHEF_ORGANIZATION", "ktor") // Optional

// HttpClient object will created lazily when invoke `ChefApiClient()`     
val nodes = ChefApiClient().get<ChefResult>("/nodes/p-postgresql-master-01")
// Fetch nested property using dot (.)
println(roles["automatic.filesystem"])

// Format result as Json string 
println(roles["automatic.filesystem"].formatted())

// Will use existing HttpClient object
val cookbooks = ChefApiClient().get<ChefResult>("/cookbooks")
println(cookbooks["apache2"])

```

### As Ktor Client Feature
You can install this as [Ktor Client] [Feature]. This Feature will also require `JsonFeature` to be installed.
```kotlin
val client = HttpClient(HttpClientEngine) {
    install(ChefClientFeature) {
       userId = "<CHEF_USER_ID"
       userPemReader = FileReader("<USER_PEM_LOCATION>") // You can use other java.io.Reader. The reader must be open, and will be closed after use.
       serverHost = "<CHEF_SERVER_HOST>"
       organization = "<CHEF_ORGANIZATION>" // Optional, no default
       chefVersion = "<CHEF_SERVER_VERSION>" // Optional, default "12.22.5" 
    }
}
```

See [ChefApiClient.kt] as reference.



[Ktor Client]: http://ktor.io/clients/index.html
[Feature]: http://ktor.io/clients/http-client/features.html
[Chef Server Api]: https://docs.chef.io/api_chef_server.html 
[Typesafe Config]: https://lightbend.github.io/config/
[ChefApiClient.kt]: ./src/main/kotlin/id/jasoet/ktor/client/features/chef/ChefApiClient.kt

