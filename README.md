# Ktor Client Chef 

[Ktor Client] [Feature] to enable authentication mechanism to [Chef Server API].

Accessing Chef Server API [require](https://docs.chef.io/api_chef_server.html#requirements) detailed and complicated auth mechanism. 
This Kotlin library wrap Chef Server API authentication as Ktor Client Feature.

## Requirement
- Chef server user account with private key in `pem` format. 
- Java 8 or later 

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

If you set `CHEF_ORGANIZATION` url for request will prefixed by `/organizations/<NAME>/` so you don't need to include that on your request. 


#### Example
```kotlin
// Set config
System.setProperty("USER_ID", "chef-client")
System.setProperty("USER_PEM_LOCATION", "/home/chef-client/.chef/client.pem")
System.setProperty("CHEF_SERVER_HOST", "https://chef-server.jasoet.id")
// Optional
System.setProperty("CHEF_ORGANIZATION", "ktor")

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

