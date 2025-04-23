# Spring Boot 静态资源代理托管
## 1、前台项目打包
### 1.1、打包项目
`vue.config.js` 文件添加 `publicPath` 公共请求路径，访问后台时需要该公共路径映射资源
```js
module.exports = defineConfig({
  transpileDependencies: true,
  lintOnSave: false,
  publicPath: '/project1/'
});
```
### 1.2、前台资源存放目录
`D:/vue-projects` 目录下存放项目资源，项目资源目录结构如下：
![img.png](img.png)
样例vue3项目静态资源文件：[dist.rar](..%2F..%2Fvue-projects%2Fproject1%2Fdist.rar)

## 2、Spring Boot 配置
### 2.1、映射静态资源
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
            .allowCredentials(true)
            .maxAge(3600)
            .allowedHeaders("*");
  }
  
  private static final String BASE_DIR = "D:/vue-projects";

  @Override
  public void addResourceHandlers(@NotNull ResourceHandlerRegistry registry) {
    File baseDir = new File(BASE_DIR);
    if (baseDir.exists() && baseDir.isDirectory()) {
      for (File projectDir : Objects.requireNonNull(baseDir.listFiles(File::isDirectory))) {
        String projectName = projectDir.getName();
        //  dist 文件夹下
        File distDir = new File(projectDir, "dist");
        if (distDir.exists()) {
          registerProject(registry, projectName, distDir);
        }
      }
    }
  }

  private void registerProject(ResourceHandlerRegistry registry, String projectName, File distDir) {
    // 访问路径映射  /projectName/** ， 映射到 dist 目录下
    String resourcePath = "/" + projectName + "/**";
    String location = "file:" + distDir.getAbsolutePath() + "/";
    registry.addResourceHandler(resourcePath)
            .addResourceLocations(location)
            .resourceChain(true)
            .addResolver(new PathResourceResolver() {
              @Override
              protected Resource getResource(@NotNull String resourcePath, @NotNull Resource location) throws IOException {
                Resource resource = location.createRelative(resourcePath);
                // 不存在该资源，默认返回 index.html
                return resource.exists() ? resource : location.createRelative("index.html");
              }
            });
  }
}
```
### 2.2、声明接口，请求转发
```java
@RestController
public class CallbackController {
    @GetMapping("/project")
    public ResponseEntity<?> loadProject(@RequestParam String projectName) {
        File distDir = new File("D:/vue-projects/" + projectName + "/dist");
        if (!distDir.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(302)
                .location(URI.create("/" + projectName + "/login"))
                .build();
    }
}
```

```xml
    <!-- spring boot web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
```
### 验证
![img_1.png](img_1.png)
