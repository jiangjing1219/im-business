package com.jiangjing.im.app.bussiness.test;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 项目信息缓存管理器
 * 支持启动时批量预热缓存，运行时按需加载
 */
public class ProjectCacheManager {

    // 项目信息服务（假设的接口）
    private final ProjectService projectService;

    // 主缓存：使用Caffeine实现高性能本地缓存
    private final LoadingCache<String, ProjectInfo> cache;

    // 正在加载中的项目锁，防止缓存击穿
    private final ConcurrentHashMap<String, ReentrantLock> loadingLocks = new ConcurrentHashMap<>();

    // 缓存配置
    private final CacheConfig config;

    public ProjectCacheManager(ProjectService projectService, CacheConfig config) {
        this.projectService = projectService;
        this.config = config;

        // 初始化缓存
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(config.getExpireAfterWriteMinutes(), TimeUnit.MINUTES)
                .maximumSize(config.getMaximumSize())
                .refreshAfterWrite(config.getRefreshAfterWriteMinutes(), TimeUnit.MINUTES)
                .build(new CacheLoader<String, ProjectInfo>() {
                    @Override
                    public @Nullable ProjectInfo load(@NonNull String projectId) throws Exception {
                        return loadProject(projectId);
                    }
                });
    }

    /**
     * 项目启动时批量预热缓存
     * @param projectIds 项目ID列表
     */
    public void warmUpCache(List<String> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return;
        }

        try {
            // 分批处理，避免单次请求数据量过大
            int batchSize = config.getBatchSize();
            for (int i = 0; i < projectIds.size(); i += batchSize) {
                List<String> batchIds = projectIds.subList(i, Math.min(i + batchSize, projectIds.size()));

                // 批量加载项目信息
                Map<String, ProjectInfo> batchProjects = projectService.batchGetProjectInfo(batchIds);

                // 放入缓存
                cache.putAll(batchProjects);

                // 记录未找到的项目ID，避免缓存穿透
                batchIds.stream()
                        .filter(id -> !batchProjects.containsKey(id))
                        .forEach(id -> cache.put(id, ProjectInfo.NOT_FOUND));
            }
        } catch (Exception e) {
            // 记录日志，但不中断启动流程
            System.err.println("预热缓存失败: " + e.getMessage());
        }
    }

    /**
     * 根据项目ID获取项目信息
     * @param projectId 项目ID
     * @return 项目信息，如果不存在返回null
     */
    public ProjectInfo getProjectInfo(String projectId) {
        if (projectId == null || projectId.trim().isEmpty()) {
            return null;
        }

        try {
            ProjectInfo project = cache.get(projectId);

            // 处理标记为未找到的项目
            if (ProjectInfo.NOT_FOUND.equals(project)) {
                return null;
            }

            return project;
        } catch (Exception e) {
            // 缓存加载失败，降级到直接查询
            System.err.println("从缓存加载项目信息失败，降级查询: " + e.getMessage());
            return loadProjectWithFallback(projectId);
        }
    }

    /**
     * 批量获取项目信息
     * @param projectIds 项目ID列表
     * @return 项目信息映射表
     */
    public Map<String, ProjectInfo> batchGetProjectInfo(List<String> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 去重
        Set<String> uniqueIds = new HashSet<>(projectIds);

        try {
            // 先从缓存中获取所有能获取到的项目
            Map<String, ProjectInfo> result = new HashMap<>();
            List<String> missingIds = new ArrayList<>();

            for (String id : uniqueIds) {
                ProjectInfo project = cache.getIfPresent(id);
                if (project != null) {
                    if (!ProjectInfo.NOT_FOUND.equals(project)) {
                        result.put(id, project);
                    }
                } else {
                    missingIds.add(id);
                }
            }

            // 如果有未命中的项目，批量加载
            if (!missingIds.isEmpty()) {
                Map<String, ProjectInfo> missingProjects = batchLoadProjects(missingIds);
                result.putAll(missingProjects);

                // 更新缓存
                cache.putAll(missingProjects);

                // 标记未找到的项目
                missingIds.stream()
                        .filter(id -> !missingProjects.containsKey(id))
                        .forEach(id -> cache.put(id, ProjectInfo.NOT_FOUND));
            }

            return result;
        } catch (Exception e) {
            // 降级处理：逐个查询
            System.err.println("批量获取项目信息失败，降级处理: " + e.getMessage());
            return batchGetWithFallback(uniqueIds);
        }
    }

    /**
     * 手动刷新缓存中的项目信息
     * @param projectId 项目ID
     */
    public void refreshProject(String projectId) {
        if (projectId != null) {
            cache.refresh(projectId);
        }
    }

    /**
     * 从缓存中移除项目信息
     * @param projectId 项目ID
     */
    public void removeProject(String projectId) {
        if (projectId != null) {
            cache.invalidate(projectId);
        }
    }

    /**
     * 清空缓存
     */
    public void clearCache() {
        cache.invalidateAll();
    }

    /**
     * 获取缓存统计信息
     */
    public String getCacheStats() {
        return cache.stats().toString();
    }

    // 私有方法实现

    /**
     * 单个项目加载（缓存加载器使用）
     */
    private ProjectInfo loadProject(String projectId) {
        ReentrantLock lock = loadingLocks.computeIfAbsent(projectId, k -> new ReentrantLock());
        lock.lock();

        try {
            // 双重检查，防止并发重复加载
            ProjectInfo cached = cache.getIfPresent(projectId);
            if (cached != null && !ProjectInfo.NOT_FOUND.equals(cached)) {
                return cached;
            }

            // 调用服务获取项目信息
            ProjectInfo project = projectService.getProjectInfo(projectId);
            if (project == null) {
                // 使用特殊标记避免缓存穿透
                return ProjectInfo.NOT_FOUND;
            }

            return project;
        } finally {
            lock.unlock();
            loadingLocks.remove(projectId);
        }
    }

    /**
     * 批量项目加载（缓存加载器使用）
     */
    private Map<String, ProjectInfo> batchLoadProjects(Iterable<? extends String> projectIds) {
        List<String> idList = new ArrayList<>();
        for (String id : projectIds) {
            idList.add(id);
        }

        if (idList.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            // 调用批量查询接口
            return projectService.batchGetProjectInfo(idList);
        } catch (Exception e) {
            // 批量查询失败，降级为逐个查询
            System.err.println("批量加载项目失败，降级处理: " + e.getMessage());
            return loadProjectsIndividually(idList);
        }
    }

    /**
     * 逐个加载项目（降级方案）
     */
    private Map<String, ProjectInfo> loadProjectsIndividually(List<String> projectIds) {
        Map<String, ProjectInfo> result = new HashMap<>();

        for (String id : projectIds) {
            try {
                ProjectInfo project = projectService.getProjectInfo(id);
                if (project != null) {
                    result.put(id, project);
                }
            } catch (Exception e) {
                System.err.println("加载项目 " + id + " 失败: " + e.getMessage());
            }
        }

        return result;
    }

    /**
     * 单个项目降级查询
     */
    private ProjectInfo loadProjectWithFallback(String projectId) {
        ReentrantLock lock = loadingLocks.computeIfAbsent(projectId, k -> new ReentrantLock());
        lock.lock();

        try {
            // 双重检查
            ProjectInfo cached = cache.getIfPresent(projectId);
            if (cached != null) {
                return ProjectInfo.NOT_FOUND.equals(cached) ? null : cached;
            }

            // 直接调用服务
            ProjectInfo project = projectService.getProjectInfo(projectId);
            if (project != null) {
                cache.put(projectId, project);
                return project;
            } else {
                // 标记未找到
                cache.put(projectId, ProjectInfo.NOT_FOUND);
                return null;
            }
        } finally {
            lock.unlock();
            loadingLocks.remove(projectId);
        }
    }

    /**
     * 批量项目降级查询
     */
    private Map<String, ProjectInfo> batchGetWithFallback(Set<String> projectIds) {
        Map<String, ProjectInfo> result = new HashMap<>();

        for (String id : projectIds) {
            ProjectInfo project = loadProjectWithFallback(id);
            if (project != null) {
                result.put(id, project);
            }
        }

        return result;
    }

    // 配置类
    public static class CacheConfig {
        private long expireAfterWriteMinutes = 30; // 缓存过期时间
        private long refreshAfterWriteMinutes = 20; // 缓存刷新时间
        private long maximumSize = 10000; // 最大缓存数量
        private int batchSize = 100; // 批量操作大小

        // getters and setters
        public long getExpireAfterWriteMinutes() { return expireAfterWriteMinutes; }
        public void setExpireAfterWriteMinutes(long expireAfterWriteMinutes) { this.expireAfterWriteMinutes = expireAfterWriteMinutes; }

        public long getRefreshAfterWriteMinutes() { return refreshAfterWriteMinutes; }
        public void setRefreshAfterWriteMinutes(long refreshAfterWriteMinutes) { this.refreshAfterWriteMinutes = refreshAfterWriteMinutes; }

        public long getMaximumSize() { return maximumSize; }
        public void setMaximumSize(long maximumSize) { this.maximumSize = maximumSize; }

        public int getBatchSize() { return batchSize; }
        public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
    }

    // 假设的项目信息类
    public static class ProjectInfo {
        public static final ProjectInfo NOT_FOUND = new ProjectInfo();

        private String projectId;
        private String projectName;
        // 其他项目属性...

        // getters and setters
        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }

        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProjectInfo that = (ProjectInfo) o;
            return Objects.equals(projectId, that.projectId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(projectId);
        }
    }

    // 假设的项目服务接口
    public interface ProjectService {
        ProjectInfo getProjectInfo(String projectId);
        Map<String, ProjectInfo> batchGetProjectInfo(List<String> projectIds);
    }

    public static void main(String[] args) {
        // 初始化缓存管理器
        ProjectCacheManager.CacheConfig config = new ProjectCacheManager.CacheConfig();
        config.setExpireAfterWriteMinutes(30);
        config.setMaximumSize(5000);
        config.setBatchSize(50);

        ProjectCacheManager cacheManager = new ProjectCacheManager(new ProjectService() {
            @Override
            public ProjectInfo getProjectInfo(String projectId) {
                return null;
            }

            @Override
            public Map<String, ProjectInfo> batchGetProjectInfo(List<String> projectIds) {
                return Collections.emptyMap();
            }
        }, config);

// 项目启动时预热缓存
        List<String> initialProjectIds = Arrays.asList("proj1", "proj2", "proj3");
        cacheManager.warmUpCache(initialProjectIds);

// 业务执行过程中获取项目信息
        ProjectInfo project = cacheManager.getProjectInfo("proj1");
        if (project != null) {
            // 使用项目信息
            System.out.println("项目名称: " + project.getProjectName());
        }

// 批量获取项目信息
        Map<String, ProjectInfo> projects = cacheManager.batchGetProjectInfo(
                Arrays.asList("proj1", "proj2", "proj4"));
    }
}
