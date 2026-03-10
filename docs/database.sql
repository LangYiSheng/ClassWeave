-- ClassWeave 数据库初始化脚本
-- 目标数据库：MySQL 8.x

CREATE DATABASE IF NOT EXISTS classweave
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE classweave;

SET NAMES utf8mb4;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  username VARCHAR(50) NOT NULL COMMENT '登录账号，唯一',
  password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
  display_name VARCHAR(50) DEFAULT NULL COMMENT '显示名称',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username)
) ENGINE=InnoDB COMMENT='用户表';

-- 课程表主表
CREATE TABLE IF NOT EXISTS schedules (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  owner_user_id BIGINT UNSIGNED NOT NULL COMMENT '创建者用户 ID',
  name VARCHAR(100) NOT NULL COMMENT '课程表名称',
  term_label VARCHAR(100) DEFAULT NULL COMMENT '学期标签，例如 2026 春',
  description VARCHAR(255) DEFAULT NULL COMMENT '描述',
  start_date DATE NOT NULL COMMENT '开学第一天日期',
  total_weeks TINYINT UNSIGNED NOT NULL DEFAULT 18 COMMENT '总周数',
  max_periods_per_day TINYINT UNSIGNED NOT NULL DEFAULT 12 COMMENT '每天最大节次数',
  default_color VARCHAR(20) DEFAULT '#1F6FEB' COMMENT '默认展示颜色',
  is_archived TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否归档',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_schedules_owner_user_id (owner_user_id),
  CONSTRAINT fk_schedules_owner_user_id
    FOREIGN KEY (owner_user_id) REFERENCES users (id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='课程表主表';

-- 分享链接表
CREATE TABLE IF NOT EXISTS schedule_share_links (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  schedule_id BIGINT UNSIGNED NOT NULL COMMENT '课程表 ID',
  created_by_user_id BIGINT UNSIGNED NOT NULL COMMENT '创建人用户 ID',
  share_token CHAR(32) NOT NULL COMMENT '分享令牌，建议后端生成 32 位随机字符串',
  permission_type ENUM('VIEW') NOT NULL DEFAULT 'VIEW' COMMENT '当前阶段只支持只读分享',
  is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否有效',
  expires_at DATETIME DEFAULT NULL COMMENT '过期时间，为空表示不过期',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_schedule_share_links_share_token (share_token),
  KEY idx_schedule_share_links_schedule_id (schedule_id),
  CONSTRAINT fk_schedule_share_links_schedule_id
    FOREIGN KEY (schedule_id) REFERENCES schedules (id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT fk_schedule_share_links_created_by_user_id
    FOREIGN KEY (created_by_user_id) REFERENCES users (id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='课程表分享链接表';

-- 用户对课程表的可访问关系
-- OWNER：自己的课表
-- VIEWER：通过分享链接加入后的只读课表
CREATE TABLE IF NOT EXISTS schedule_accesses (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户 ID',
  schedule_id BIGINT UNSIGNED NOT NULL COMMENT '课程表 ID',
  access_role ENUM('OWNER', 'VIEWER') NOT NULL DEFAULT 'VIEWER' COMMENT '访问角色',
  access_source ENUM('OWNER', 'SHARE_LINK') NOT NULL DEFAULT 'SHARE_LINK' COMMENT '访问来源',
  share_link_id BIGINT UNSIGNED DEFAULT NULL COMMENT '来自哪个分享链接，OWNER 时为空',
  display_name_override VARCHAR(100) DEFAULT NULL COMMENT '显示名称覆盖',
  display_color VARCHAR(20) DEFAULT NULL COMMENT '显示颜色覆盖',
  display_opacity DECIMAL(4,2) NOT NULL DEFAULT 0.85 COMMENT '默认透明度',
  is_visible_default TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否默认显示',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_schedule_accesses_user_schedule (user_id, schedule_id),
  KEY idx_schedule_accesses_schedule_id (schedule_id),
  CONSTRAINT fk_schedule_accesses_user_id
    FOREIGN KEY (user_id) REFERENCES users (id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT fk_schedule_accesses_schedule_id
    FOREIGN KEY (schedule_id) REFERENCES schedules (id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT fk_schedule_accesses_share_link_id
    FOREIGN KEY (share_link_id) REFERENCES schedule_share_links (id)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='用户可访问课程表关系表';

-- 每张课程表的节次时间定义
CREATE TABLE IF NOT EXISTS schedule_time_slots (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  schedule_id BIGINT UNSIGNED NOT NULL COMMENT '课程表 ID',
  period_index TINYINT UNSIGNED NOT NULL COMMENT '第几节，从 1 开始',
  start_time TIME NOT NULL COMMENT '开始时间',
  end_time TIME NOT NULL COMMENT '结束时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_schedule_time_slots_schedule_period (schedule_id, period_index),
  CONSTRAINT fk_schedule_time_slots_schedule_id
    FOREIGN KEY (schedule_id) REFERENCES schedules (id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='课程表节次时间定义';

-- 课程记录表
CREATE TABLE IF NOT EXISTS schedule_courses (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  schedule_id BIGINT UNSIGNED NOT NULL COMMENT '课程表 ID',
  name VARCHAR(100) NOT NULL COMMENT '课程名称',
  weekday TINYINT UNSIGNED NOT NULL COMMENT '星期几，1-7 表示周一到周日',
  start_week TINYINT UNSIGNED NOT NULL COMMENT '开始周',
  end_week TINYINT UNSIGNED NOT NULL COMMENT '结束周',
  start_period TINYINT UNSIGNED NOT NULL COMMENT '开始节次',
  end_period TINYINT UNSIGNED NOT NULL COMMENT '结束节次',
  week_type ENUM('ALL', 'ODD', 'EVEN') NOT NULL DEFAULT 'ALL' COMMENT '周类型',
  teacher VARCHAR(100) DEFAULT NULL COMMENT '教师姓名',
  location VARCHAR(100) DEFAULT NULL COMMENT '上课地点',
  note VARCHAR(500) DEFAULT NULL COMMENT '备注',
  is_temporary TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否临时课程',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_schedule_courses_schedule_day_week (schedule_id, weekday, start_week, end_week),
  CONSTRAINT fk_schedule_courses_schedule_id
    FOREIGN KEY (schedule_id) REFERENCES schedules (id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='课程记录表';

-- 推荐初始化流程：
-- 1. 新建课程表时，向 schedules 插入一条记录
-- 2. 同时向 schedule_accesses 插入一条 OWNER 记录
-- 3. 再插入 schedule_time_slots 和 schedule_courses
--
-- 推荐删除流程：
-- 1. 只允许 OWNER 删除 schedules
-- 2. 依赖外键级联，time_slots / courses / share_links / accesses 会自动删除
--
-- 推荐移除共享课表流程：
-- 1. 仅 VIEWER 删除自己在 schedule_accesses 中对应的那条访问记录
