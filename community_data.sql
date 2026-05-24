-- ============================================================
-- 社区物业设备维护管理系统 - 初始化数据
-- ============================================================
USE property_system;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 清空原有数据
TRUNCATE TABLE sys_tenant;
TRUNCATE TABLE sys_role;
TRUNCATE TABLE sys_user;
TRUNCATE TABLE sys_user_role;
TRUNCATE TABLE community;
TRUNCATE TABLE building;
TRUNCATE TABLE room;
TRUNCATE TABLE property;
TRUNCATE TABLE device_category;
TRUNCATE TABLE device;
TRUNCATE TABLE repair_order_flow;
TRUNCATE TABLE repair_order;
TRUNCATE TABLE repair_order_log;
TRUNCATE TABLE repair_order_evaluation;
TRUNCATE TABLE repair_order_material;
TRUNCATE TABLE inspection_plan;
TRUNCATE TABLE inspection_task;
TRUNCATE TABLE inspection_record;
TRUNCATE TABLE notice;
TRUNCATE TABLE notice_target;
TRUNCATE TABLE message;
TRUNCATE TABLE biz_file;
TRUNCATE TABLE sys_dict;

-- ===================== 1. 租户数据 =====================
INSERT INTO sys_tenant (id, name, status, create_time) VALUES
(1, '万科物业服务有限公司', 1, DATE_SUB(NOW(), INTERVAL 300 DAY)),
(2, '保利物业管理有限公司', 1, DATE_SUB(NOW(), INTERVAL 200 DAY)),
(3, '绿城物业服务集团', 1, DATE_SUB(NOW(), INTERVAL 150 DAY));

-- ===================== 2. 字典数据 =====================
INSERT INTO sys_dict (tenant_id, dict_type, dict_key, dict_value) VALUES
(1, 'order_status', '0', '待受理'), (1, 'order_status', '1', '待派单'), (1, 'order_status', '2', '待处理'), (1, 'order_status', '3', '处理中'), (1, 'order_status', '4', '待评价'), (1, 'order_status', '5', '已完成'), (1, 'order_status', '6', '已取消'), (1, 'order_status', '7', '已转单'),
(2, 'order_status', '0', '待受理'), (2, 'order_status', '1', '待派单'), (2, 'order_status', '2', '待处理'), (2, 'order_status', '3', '处理中'), (2, 'order_status', '4', '待评价'), (2, 'order_status', '5', '已完成'), (2, 'order_status', '6', '已取消'), (2, 'order_status', '7', '已转单'),
(3, 'order_status', '0', '待受理'), (3, 'order_status', '1', '待派单'), (3, 'order_status', '2', '待处理'), (3, 'order_status', '3', '处理中'), (3, 'order_status', '4', '待评价'), (3, 'order_status', '5', '已完成'), (3, 'order_status', '6', '已取消'), (3, 'order_status', '7', '已转单');

-- ===================== 3. 角色数据 =====================
INSERT INTO sys_role (id, tenant_id, role_code, role_name, description, status, sort_order) VALUES
(1, 1, 'ADMIN', '系统管理员', '物业公司管理员，拥有全部管理权限', 1, 1),
(2, 1, 'WORKER', '维修工', '执行维修与巡检任务', 1, 2),
(3, 1, 'OWNER', '业主', '社区居民，提交报修与评价', 1, 3),
(4, 2, 'ADMIN', '系统管理员', '物业公司管理员，拥有全部管理权限', 1, 1),
(5, 2, 'WORKER', '维修工', '执行维修与巡检任务', 1, 2),
(6, 2, 'OWNER', '业主', '社区居民，提交报修与评价', 1, 3),
(7, 3, 'ADMIN', '系统管理员', '物业公司管理员，拥有全部管理权限', 1, 1),
(8, 3, 'WORKER', '维修工', '执行维修与巡检任务', 1, 2),
(9, 3, 'OWNER', '业主', '社区居民，提交报修与评价', 1, 3);

-- ===================== 4. 房产基建数据 =====================
INSERT INTO community (id, tenant_id, name) VALUES
(1, 1, '万科星城'), (2, 1, '万科金域缇香'), 
(3, 2, '保利西岸'), (4, 2, '保利心语'), 
(5, 3, '绿城桂语江南'), (6, 3, '绿城百合新城');

INSERT INTO building (id, tenant_id, community_id, name) VALUES
(1,1,1,'星城1栋'),(2,1,1,'星城2栋'),(3,1,1,'星城3栋'),(4,1,1,'星城4栋'),
(5,1,2,'缇香A座'),(6,1,2,'缇香B座'),(7,1,2,'缇香C座'),(8,1,2,'缇香D座'),
(9,2,3,'西岸1号楼'),(10,2,3,'西岸2号楼'),(11,2,3,'西岸3号楼'),(12,2,3,'西岸4号楼'),
(13,2,4,'心语1栋'),(14,2,4,'心语2栋'),(15,2,4,'心语3栋'),(16,2,4,'心语4栋'),
(17,3,5,'桂语1幢'),(18,3,5,'桂语2幢'),(19,3,5,'桂语3幢'),(20,3,5,'桂语4幢'),
(21,3,6,'百合1幢'),(22,3,6,'百合2幢'),(23,3,6,'百合3幢'),(24,3,6,'百合4幢');

INSERT INTO room (id, tenant_id, building_id, room_no) VALUES 
(1,1,1,'101'),(2,1,1,'102'),(3,1,1,'201'),(4,1,1,'202'),(5,1,1,'301'),(6,1,1,'302'),
(7,1,2,'101'),(8,1,2,'102'),(9,1,2,'201'),(10,1,2,'202'),(11,1,2,'301'),(12,1,2,'302'),
(13,1,3,'101'),(14,1,3,'102'),(15,1,3,'201'),(16,1,3,'202'),(17,1,3,'301'),(18,1,3,'302'),
(19,1,4,'101'),(20,1,4,'102'),(21,1,4,'201'),(22,1,4,'202'),(23,1,4,'301'),(24,1,4,'302'),
(25,1,5,'101'),(26,1,5,'102'),(27,1,5,'201'),(28,1,5,'202'),(29,1,5,'301'),(30,1,5,'302'),
(31,2,9,'101'),(32,2,9,'102'),(33,2,9,'201'),(34,2,9,'202'),(35,2,9,'301'),(36,2,9,'302'),
(37,2,10,'101'),(38,2,10,'102'),(39,2,10,'201'),(40,2,10,'202'),(41,2,10,'301'),(42,2,10,'302'),
(43,2,11,'101'),(44,2,11,'102'),(45,2,11,'201'),(46,2,11,'202'),(47,2,11,'301'),(48,2,11,'302'),
(49,2,12,'101'),(50,2,12,'102'),(51,2,12,'201'),(52,2,12,'202'),(53,2,12,'301'),(54,2,12,'302'),
(55,3,17,'101'),(56,3,17,'102'),(57,3,17,'201'),(58,3,17,'202'),(59,3,17,'301'),(60,3,17,'302'),
(61,3,18,'101'),(62,3,18,'102'),(63,3,18,'201'),(64,3,18,'202'),(65,3,18,'301'),(66,3,18,'302'),
(67,3,19,'101'),(68,3,19,'102'),(69,3,19,'201'),(70,3,19,'202'),(71,3,19,'301'),(72,3,19,'302');

-- ===================== 5. 用户及权限分配 =====================
-- 超级管理员 (平台级，不属于任何租户)
INSERT INTO sys_user (id, tenant_id, username, phone, password, status, is_super_admin) VALUES
(1000, 0, '超级管理员', '13800000000', '$2a$10$GB0x1f2wyj63p0/NyxpWJet6Iz/P2EusRcaUZEh9ZWHjt6wqWBA/C', 1, 1);

INSERT INTO sys_user (id, tenant_id, username, phone, password, status) VALUES
(1, 1, '万科总管', '13800001111', '$2a$10$GB0x1f2wyj63p0/NyxpWJet6Iz/P2EusRcaUZEh9ZWHjt6wqWBA/C', 1),
(2, 2, '保利总管', '13800002222', '$2a$10$GB0x1f2wyj63p0/NyxpWJet6Iz/P2EusRcaUZEh9ZWHjt6wqWBA/C', 1),
(3, 3, '绿城总管', '13800003333', '$2a$10$GB0x1f2wyj63p0/NyxpWJet6Iz/P2EusRcaUZEh9ZWHjt6wqWBA/C', 1);

-- 维修工(负责维修与巡检)
INSERT INTO sys_user (id, tenant_id, username, phone, password, status) VALUES
(4, 1, '张师傅', '13900001001', '$2a$10$GB0x1f2wyj63p0/NyxpWJet6Iz/P2EusRcaUZEh9ZWHjt6wqWBA/C', 1),
(5, 1, '李师傅', '13900001002', '$2a$10$GB0x1f2wyj63p0/NyxpWJet6Iz/P2EusRcaUZEh9ZWHjt6wqWBA/C', 1),
(6, 1, '王师傅', '13900001003', '$2a$10$GB0x1f2wyj63p0/NyxpWJet6Iz/P2EusRcaUZEh9ZWHjt6wqWBA/C', 1),
(7, 1, '赵师傅', '13900001004', '$2a$10$GB0x1f2wyj63p0/NyxpWJet6Iz/P2EusRcaUZEh9ZWHjt6wqWBA/C', 1),
(8, 2, '刘师傅', '13900002001', '$2a$10$GB0x1f2wyj63p0/NyxpWJet6Iz/P2EusRcaUZEh9ZWHjt6wqWBA/C', 1),
(9, 2, '陈师傅', '13900002002', '$2a$10$GB0x1f2wyj63p0/NyxpWJet6Iz/P2EusRcaUZEh9ZWHjt6wqWBA/C', 1),
(10, 2, '林师傅', '13900002003', '$2a$10$GB0x1f2wyj63p0/NyxpWJet6Iz/P2EusRcaUZEh9ZWHjt6wqWBA/C', 1),
(11, 2, '黄师傅', '13900002004', '$2a$10$GB0x1f2wyj63p0/NyxpWJet6Iz/P2EusRcaUZEh9ZWHjt6wqWBA/C', 1),
(12, 3, '吴师傅', '13900003001', '$2a$10$GB0x1f2wyj63p0/NyxpWJet6Iz/P2EusRcaUZEh9ZWHjt6wqWBA/C', 1),
(13, 3, '郑师傅', '13900003002', '$2a$10$GB0x1f2wyj63p0/NyxpWJet6Iz/P2EusRcaUZEh9ZWHjt6wqWBA/C', 1),
(14, 3, '孙师傅', '13900003003', '$2a$10$GB0x1f2wyj63p0/NyxpWJet6Iz/P2EusRcaUZEh9ZWHjt6wqWBA/C', 1),
(15, 3, '周师傅', '13900003004', '$2a$10$GB0x1f2wyj63p0/NyxpWJet6Iz/P2EusRcaUZEh9ZWHjt6wqWBA/C', 1);

-- 业主
INSERT INTO sys_user (id, tenant_id, username, phone, password, status)
SELECT 100+id, tenant_id, CONCAT('业主_', room_no), CONCAT('158000', LPAD(id, 5, '0')), '$2a$10$GB0x1f2wyj63p0/NyxpWJet6Iz/P2EusRcaUZEh9ZWHjt6wqWBA/C', 1
FROM room;

-- 用户绑定角色
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1,1), (2,4), (3,7),
(4,2), (5,2), (6,2), (7,2),
(8,5), (9,5), (10,5), (11,5),
(12,8), (13,8), (14,8), (15,8);

-- 批量为业主绑定角色
INSERT INTO sys_user_role (user_id, role_id)
SELECT id, CASE WHEN tenant_id=1 THEN 3 WHEN tenant_id=2 THEN 6 ELSE 9 END FROM sys_user WHERE id > 100;

-- 绑定房屋与业主关系
INSERT INTO property (tenant_id, room_id, owner_id)
SELECT tenant_id, id, 100+id FROM room;

-- ===================== 6. 设备及分类数据 =====================
INSERT INTO device_category (id, tenant_id, name) VALUES
(1,1,'电梯设备'), (2,1,'消防设施'), (3,1,'安防监控'), (4,1,'给排水系统'),
(5,2,'电梯设备'), (6,2,'消防设施'), (7,2,'安防监控'), (8,2,'给排水系统'),
(9,3,'电梯设备'),(10,3,'消防设施'),(11,3,'安防监控'),(12,3,'给排水系统');

INSERT INTO device (id, tenant_id, category_id, building_id, name, model, location, status, install_date) VALUES
(1,1,1,1,'1栋客梯A', '日立 MCA', '1栋1单元电梯井', 1, '2023-01-10'),
(2,1,1,1,'1栋客梯B', '日立 MCA', '1栋1单元电梯井', 1, '2023-01-10'),
(3,1,2,1,'1栋消防泵', '格兰富 CR', '1栋地下2层泵房', 1, '2022-11-15'),
(4,1,3,1,'1栋大门监控', '海康威视 DS-2CD', '1栋主出入口', 1, '2023-03-20'),
(5,1,4,1,'1栋生活水泵', '南方泵业 CDL', '1栋地下负一层', 1, '2023-02-14'),
(6,1,1,2,'2栋客梯A', '奥的斯 Gen2', '2栋大堂', 2, '2023-05-01'), 
(7,1,2,2,'2栋消火栓', '国泰 MY', '2栋各楼层步梯', 1, '2022-09-08'),
(8,2,5,13,'心语1栋客梯', '三菱 LEHY', '心语1栋大堂', 1, '2021-06-18'),
(9,2,7,13,'心语地库监控', '大华 DH-IPC', '心语地下车库B区', 1, '2021-07-22'),
(10,3,9,17,'桂语1幢电梯', '通力 MiniSpace', '桂语1幢电梯间', 3, '2024-01-11');

-- 批量扩展生成设备
INSERT INTO device (tenant_id, category_id, building_id, name, model, location, status, install_date)
SELECT 1, 3, id, CONCAT(name, '楼层监控'), '海康威视', CONCAT(name, '步梯口'), 1, DATE_SUB(NOW(), INTERVAL 400 DAY) FROM building WHERE tenant_id = 1 UNION ALL
SELECT 1, 2, id, CONCAT(name, '灭火器箱'), '火焰山', CONCAT(name, '走廊'), 1, DATE_SUB(NOW(), INTERVAL 500 DAY) FROM building WHERE tenant_id = 1 UNION ALL
SELECT 2, 7, id, CONCAT(name, '球机监控'), '大华星光', CONCAT(name, '外围'), 1, DATE_SUB(NOW(), INTERVAL 300 DAY) FROM building WHERE tenant_id = 2 UNION ALL
SELECT 3, 9, id, CONCAT(name, '货梯'), '蒂森克虏伯', CONCAT(name, '后勤通道'), 1, DATE_SUB(NOW(), INTERVAL 100 DAY) FROM building WHERE tenant_id = 3;

-- ===================== 7. 巡检计划与任务 (由维修工负责) =====================
INSERT INTO inspection_plan (id, tenant_id, name, building_id, category_id, device_id, cycle, next_time, status, create_time) VALUES
(1, 1, '1栋电梯周检', 1, 1, 1, 7, DATE_ADD(NOW(), INTERVAL 2 DAY), 1, DATE_SUB(NOW(), INTERVAL 60 DAY)),
(2, 1, '1栋大门监控月检', 1, 3, 4, 30, DATE_ADD(NOW(), INTERVAL 10 DAY), 1, DATE_SUB(NOW(), INTERVAL 60 DAY)),
(3, 1, '2栋消防月检', 2, 2, 7, 30, DATE_ADD(NOW(), INTERVAL 5 DAY), 1, DATE_SUB(NOW(), INTERVAL 60 DAY)),
(4, 2, '心语监控巡检', 13, 7, 9, 15, DATE_ADD(NOW(), INTERVAL 1 DAY), 1, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(5, 3, '桂语电梯保养', 17, 9, 10, 15, NOW(), 1, DATE_SUB(NOW(), INTERVAL 10 DAY));

INSERT INTO inspection_task (id, tenant_id, plan_id, plan_name, building_id, category_id, device_id, assigned_to, task_date, status) VALUES
(1, 1, 1, '1栋电梯周检', 1, 1, 1, 4, DATE_SUB(NOW(), INTERVAL 7 DAY), 1),
(2, 1, 1, '1栋电梯周检', 1, 1, 1, 5, DATE_SUB(NOW(), INTERVAL 14 DAY), 1),
(3, 1, 3, '2栋消防月检', 2, 2, 7, 6, DATE_SUB(NOW(), INTERVAL 20 DAY), 1),
(4, 2, 4, '心语监控巡检', 13, 7, 9, 8, DATE_SUB(NOW(), INTERVAL 15 DAY), 1),
(5, 3, 5, '桂语电梯保养', 17, 9, 10, 12, DATE_SUB(NOW(), INTERVAL 15 DAY), 1),
(6, 1, 1, '1栋电梯周检', 1, 1, 1, 4, DATE_ADD(NOW(), INTERVAL 2 DAY), 0),
(7, 1, 3, '2栋消防月检', 2, 2, 7, 6, DATE_ADD(NOW(), INTERVAL 5 DAY), 0),
(8, 3, 5, '桂语电梯保养', 17, 9, 10, 13, NOW(), 0);

INSERT INTO inspection_record (tenant_id, task_id, device_id, result, remark, create_time) VALUES
(1, 1, 1, 1, '运行平稳，无异响', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(1, 2, 1, 1, '照明正常', DATE_SUB(NOW(), INTERVAL 14 DAY)),
(1, 3, 7, 0, '压力偏低', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(2, 4, 9, 1, '画面清晰', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(3, 5, 10, 1, '常规保养完成', DATE_SUB(NOW(), INTERVAL 15 DAY));

-- ===================== 8. 工单全流程数据 =====================
INSERT INTO repair_order_flow (tenant_id, from_status, to_status, action) VALUES
-- 标准流程
(1,0,1,'审核通过'), (1,1,2,'派单'), (1,2,3,'开始处理'), (1,3,4,'完成维修'), (1,4,5,'用户评价'),
-- 取消流程
(1,0,6,'取消报修'), (1,0,6,'拒绝'), (1,1,6,'管理员取消'), (1,2,6,'管理员取消'),
-- 转派流程
(1,2,2,'转派'), (1,3,2,'转派'), (1,2,7,'申请转单'), (1,7,2,'重新派单'),
-- 租户2
(2,0,1,'审核通过'), (2,1,2,'派单'), (2,2,3,'开始处理'), (2,3,4,'完成维修'), (2,4,5,'用户评价'),
(2,0,6,'取消报修'), (2,0,6,'拒绝'), (2,1,6,'管理员取消'), (2,2,6,'管理员取消'),
(2,2,2,'转派'), (2,3,2,'转派'), (2,2,7,'申请转单'), (2,7,2,'重新派单'),
-- 租户3
(3,0,1,'审核通过'), (3,1,2,'派单'), (3,2,3,'开始处理'), (3,3,4,'完成维修'), (3,4,5,'用户评价'),
(3,0,6,'取消报修'), (3,0,6,'拒绝'), (3,1,6,'管理员取消'), (3,2,6,'管理员取消'),
(3,2,2,'转派'), (3,3,2,'转派'), (3,2,7,'申请转单'), (3,7,2,'重新派单');

INSERT INTO repair_order (id, tenant_id, order_no, user_id, device_id, address, fault_desc, status, priority, assign_to, appoint_time, finish_time, process_desc, create_time) VALUES
(1, 1, 'WO202605001', 101, 1, '星城1栋1单元', '电梯按键不灵敏', 5, 2, 4, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), '已更换开关', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(2, 1, 'WO202605002', 102, 5, '星城1栋地下室', '生活水泵异响', 5, 3, 5, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY), '已更换轴承', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(3, 1, 'WO202605003', 103, NULL, '星城1栋201', '主卧窗户漏水', 4, 1, 4, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 2 HOUR), '已打胶', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 2, 'WO202605004', 131, 8, '心语1栋', '客梯停在3楼不动', 3, 3, 8, NOW(), NULL, NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(5, 3, 'WO202605005', 155, NULL, '桂语1幢101', '可视对讲黑屏', 2, 2, 12, DATE_ADD(NOW(), INTERVAL 1 DAY), NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(6, 1, 'WO202605006', 104, NULL, '星城1栋202', '入户门锁发卡', 1, 1, NULL, NULL, NULL, NULL, NOW());

INSERT INTO repair_order_log (tenant_id, order_id, operator_id, action, remark, create_time) VALUES
(1, 1, 101, '提交报修', '业主提交', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(1, 1, 1, '派单', '指派给张师傅', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, 1, 4, '完成维修', '更换配件', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 1, 101, '评价工单', '五星好评', DATE_SUB(NOW(), INTERVAL 3 DAY));

INSERT INTO repair_order_evaluation (tenant_id, order_id, rating, comment, create_time) VALUES
(1, 1, 5, '张师傅上门很快', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 2, 4, '修好了', DATE_SUB(NOW(), INTERVAL 8 DAY));

INSERT INTO repair_order_material (tenant_id, order_id, material_name, quantity, create_time) VALUES
(1, 1, '微动开关', 2, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 2, '轴承', 1, DATE_SUB(NOW(), INTERVAL 9 DAY));

-- ===================== 9. 公告及消息 (移除阅读统计) =====================
INSERT INTO notice (id, tenant_id, title, content, publish_status, scheduled_time, create_time) VALUES
(1, 1, '停水通知', '明日上午9:00至下午17:00停水。', 1, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 2, '防台风预警', '近两日有大暴雨，请注意防范。', 1, NULL, DATE_SUB(NOW(), INTERVAL 12 HOUR)),
(3, 3, '消杀通知', '本周五下午集中灭蚊消杀。', 0, DATE_ADD(NOW(), INTERVAL 2 DAY), NOW());

INSERT INTO notice_target (tenant_id, notice_id, building_id) VALUES
(1, 1, 1), (2, 2, 9), (3, 3, 17);

INSERT INTO message (tenant_id, user_id, type, content, is_read, create_time) VALUES
(1, 101, 'ORDER_UPDATE', '工单已完成。', 1, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 4,   'NEW_ORDER', '有新指派工单。', 1, DATE_SUB(NOW(), INTERVAL 5 DAY));

INSERT INTO message (tenant_id, user_id, type, content, is_read, create_time)
SELECT tenant_id, id, 'SYSTEM', '欢迎使用智慧物业系统！', 0, DATE_SUB(NOW(), INTERVAL 30 DAY) FROM sys_user WHERE id > 100;

-- ===================== 10. 文件记录 =====================
INSERT INTO biz_file (tenant_id, biz_type, biz_id, file_type, url, create_time) VALUES
(1, 'device_qr', 1, 'image/png', 'https://cdn.property.com/qr/device_1.png', DATE_SUB(NOW(), INTERVAL 300 DAY)),
(1, 'repair_before', 1, 'image/jpeg', 'https://cdn.property.com/repair/wo1_before.jpg', DATE_SUB(NOW(), INTERVAL 6 DAY));

SET FOREIGN_KEY_CHECKS = 1;

-- ===================== 补充：30条工单数据 =====================
INSERT INTO repair_order (tenant_id, order_no, user_id, device_id, address, fault_desc, status, priority, assign_to, appoint_time, finish_time, process_desc, create_time) VALUES
(1, 'WO202605007', 123, 2, '测试楼栋3号 房间471', '墙皮脱落', 3, 3, 5, DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, '已打胶密封', DATE_SUB(NOW(), INTERVAL 14 DAY)),
(1, 'WO202605008', 129, 3, '测试楼栋4号 房间191', '过道灯不亮', 4, 3, 5, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), '测试已恢复正常', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(2, 'WO202605009', 151, 8, '测试楼栋12号 房间284', '水管漏水', 1, 0, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(1, 'WO202605010', 114, 5, '测试楼栋1号 房间401', '空调不制冷', 0, 3, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(1, 'WO202605011', 117, 7, '测试楼栋3号 房间172', '门禁卡失效', 3, 1, 7, DATE_SUB(NOW(), INTERVAL 7 DAY), NULL, '等待采购配件', DATE_SUB(NOW(), INTERVAL 19 DAY)),
(1, 'WO202605012', 127, NULL, '测试楼栋4号 房间210', '电灯闪烁', 3, 2, 7, DATE_SUB(NOW(), INTERVAL 9 DAY), NULL, '测试已恢复正常', DATE_SUB(NOW(), INTERVAL 11 DAY)),
(3, 'WO202605013', 170, 10, '测试楼栋19号 房间145', '空调不制冷', 3, 3, 13, DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, '已清理异物', DATE_SUB(NOW(), INTERVAL 14 DAY)),
(1, 'WO202605014', 117, 3, '测试楼栋3号 房间574', '监控黑屏', 0, 1, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 12 DAY)),
(1, 'WO202605015', 104, 5, '测试楼栋7号 房间394', '过道灯不亮', 3, 3, 6, DATE_SUB(NOW(), INTERVAL 10 DAY), NULL, '已疏通管道', DATE_SUB(NOW(), INTERVAL 12 DAY)),
(1, 'WO202605016', 103, 3, '测试楼栋3号 房间236', '门锁损坏', 5, 0, 7, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), '等待采购配件', DATE_SUB(NOW(), INTERVAL 16 DAY)),
(3, 'WO202605017', 159, 10, '测试楼栋19号 房间589', '监控黑屏', 4, 3, 12, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), '已更换损坏零件', DATE_SUB(NOW(), INTERVAL 12 DAY)),
(2, 'WO202605018', 147, NULL, '测试楼栋12号 房间524', '电梯异响', 1, 2, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 16 DAY)),
(2, 'WO202605019', 142, 9, '测试楼栋13号 房间195', '地漏堵塞', 2, 3, 8, DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, NULL, DATE_SUB(NOW(), INTERVAL 19 DAY)),
(3, 'WO202605020', 160, 10, '测试楼栋17号 房间240', '地漏堵塞', 3, 2, 13, DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, '已清理异物', DATE_SUB(NOW(), INTERVAL 13 DAY)),
(1, 'WO202605021', 123, 7, '测试楼栋1号 房间106', '电灯闪烁', 2, 2, 6, DATE_SUB(NOW(), INTERVAL 8 DAY), NULL, NULL, DATE_SUB(NOW(), INTERVAL 19 DAY)),
(3, 'WO202605022', 159, 10, '测试楼栋18号 房间560', '门禁卡失效', 3, 2, 13, DATE_SUB(NOW(), INTERVAL 7 DAY), NULL, '等待采购配件', DATE_SUB(NOW(), INTERVAL 12 DAY)),
(3, 'WO202605023', 156, NULL, '测试楼栋22号 房间566', '地漏堵塞', 3, 2, 12, DATE_SUB(NOW(), INTERVAL 8 DAY), NULL, '已清理异物', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(3, 'WO202605024', 169, 10, '测试楼栋18号 房间397', '插座无电', 4, 2, 14, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), '测试已恢复正常', DATE_SUB(NOW(), INTERVAL 11 DAY)),
(3, 'WO202605025', 164, 10, '测试楼栋23号 房间510', '电梯异响', 0, 1, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 12 DAY)),
(3, 'WO202605026', 165, NULL, '测试楼栋21号 房间432', '过道灯不亮', 4, 2, 14, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), '测试已恢复正常', DATE_SUB(NOW(), INTERVAL 17 DAY)),
(2, 'WO202605027', 151, NULL, '测试楼栋11号 房间178', '空调不制冷', 1, 3, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 17 DAY)),
(2, 'WO202605028', 147, 9, '测试楼栋16号 房间431', '电梯异响', 1, 2, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(2, 'WO202605029', 142, NULL, '测试楼栋9号 房间160', '监控黑屏', 4, 0, 8, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), '测试已恢复正常', DATE_SUB(NOW(), INTERVAL 13 DAY)),
(1, 'WO202605030', 106, 5, '测试楼栋2号 房间559', '电灯闪烁', 2, 3, 5, DATE_SUB(NOW(), INTERVAL 10 DAY), NULL, NULL, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(1, 'WO202605031', 105, NULL, '测试楼栋2号 房间343', '电灯闪烁', 6, 0, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(1, 'WO202605032', 123, 2, '测试楼栋5号 房间439', '地漏堵塞', 5, 2, 6, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), '已更换损坏零件', DATE_SUB(NOW(), INTERVAL 14 DAY)),
(3, 'WO202605033', 159, NULL, '测试楼栋20号 房间270', '电灯闪烁', 4, 3, 14, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), '等待采购配件', DATE_SUB(NOW(), INTERVAL 11 DAY)),
(2, 'WO202605034', 151, NULL, '测试楼栋10号 房间234', '电梯异响', 3, 3, 10, DATE_SUB(NOW(), INTERVAL 8 DAY), NULL, '已更换损坏零件', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(3, 'WO202605035', 169, NULL, '测试楼栋17号 房间243', '门锁损坏', 5, 2, 13, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), '重新接线处理', DATE_SUB(NOW(), INTERVAL 11 DAY)),
(2, 'WO202605036', 150, 9, '测试楼栋9号 房间165', '水管漏水', 2, 1, 8, DATE_SUB(NOW(), INTERVAL 9 DAY), NULL, NULL, DATE_SUB(NOW(), INTERVAL 17 DAY));

-- ===================== 补充：30条巡检任务与记录 =====================
INSERT INTO inspection_task (tenant_id, plan_id, plan_name, building_id, category_id, device_id, assigned_to, task_date, status) VALUES
(2, 4, '日常补充巡检计划_0', 10, 8, 8, 8, DATE_SUB(NOW(), INTERVAL -2 DAY), 0),
(1, 4, '日常补充巡检计划_1', 1, 1, 5, 5, DATE_SUB(NOW(), INTERVAL 1 DAY), 1),
(3, 3, '日常补充巡检计划_2', 19, 10, 10, 14, DATE_SUB(NOW(), INTERVAL -4 DAY), 0),
(1, 1, '日常补充巡检计划_3', 2, 1, 2, 5, DATE_SUB(NOW(), INTERVAL 8 DAY), 0),
(2, 2, '日常补充巡检计划_4', 12, 7, 8, 9, DATE_SUB(NOW(), INTERVAL -3 DAY), 0),
(3, 1, '日常补充巡检计划_5', 18, 12, 10, 14, DATE_SUB(NOW(), INTERVAL 1 DAY), 0),
(1, 1, '日常补充巡检计划_6', 7, 2, 2, 6, DATE_SUB(NOW(), INTERVAL 4 DAY), 1),
(3, 1, '日常补充巡检计划_7', 18, 11, 10, 12, DATE_SUB(NOW(), INTERVAL -3 DAY), 1),
(3, 4, '日常补充巡检计划_8', 21, 11, 10, 12, DATE_SUB(NOW(), INTERVAL -5 DAY), 0),
(3, 1, '日常补充巡检计划_9', 18, 11, 10, 13, DATE_SUB(NOW(), INTERVAL -4 DAY), 1),
(2, 2, '日常补充巡检计划_10', 9, 8, 8, 10, DATE_SUB(NOW(), INTERVAL 0 DAY), 1),
(1, 1, '日常补充巡检计划_11', 4, 1, 7, 7, DATE_SUB(NOW(), INTERVAL -2 DAY), 1),
(1, 3, '日常补充巡检计划_12', 3, 2, 3, 7, DATE_SUB(NOW(), INTERVAL -1 DAY), 0),
(3, 4, '日常补充巡检计划_13', 24, 10, 10, 12, DATE_SUB(NOW(), INTERVAL -3 DAY), 1),
(2, 5, '日常补充巡检计划_14', 12, 7, 9, 8, DATE_SUB(NOW(), INTERVAL 7 DAY), 1),
(1, 4, '日常补充巡检计划_15', 5, 3, 2, 4, DATE_SUB(NOW(), INTERVAL 6 DAY), 0),
(3, 5, '日常补充巡检计划_16', 22, 10, 10, 15, DATE_SUB(NOW(), INTERVAL -2 DAY), 1),
(3, 2, '日常补充巡检计划_17', 22, 12, 10, 13, DATE_SUB(NOW(), INTERVAL 1 DAY), 0),
(1, 4, '日常补充巡检计划_18', 3, 1, 3, 4, DATE_SUB(NOW(), INTERVAL -5 DAY), 1),
(3, 3, '日常补充巡检计划_19', 17, 10, 10, 12, DATE_SUB(NOW(), INTERVAL -1 DAY), 1),
(3, 3, '日常补充巡检计划_20', 20, 10, 10, 13, DATE_SUB(NOW(), INTERVAL -4 DAY), 0),
(2, 4, '日常补充巡检计划_21', 13, 8, 9, 8, DATE_SUB(NOW(), INTERVAL 3 DAY), 1),
(3, 3, '日常补充巡检计划_22', 17, 9, 10, 14, DATE_SUB(NOW(), INTERVAL -5 DAY), 1),
(2, 5, '日常补充巡检计划_23', 15, 8, 9, 9, DATE_SUB(NOW(), INTERVAL 10 DAY), 0),
(2, 3, '日常补充巡检计划_24', 9, 5, 8, 10, DATE_SUB(NOW(), INTERVAL 5 DAY), 1),
(3, 3, '日常补充巡检计划_25', 21, 10, 10, 14, DATE_SUB(NOW(), INTERVAL 0 DAY), 1),
(3, 2, '日常补充巡检计划_26', 17, 9, 10, 15, DATE_SUB(NOW(), INTERVAL -5 DAY), 0),
(3, 2, '日常补充巡检计划_27', 17, 9, 10, 15, DATE_SUB(NOW(), INTERVAL 3 DAY), 1),
(2, 2, '日常补充巡检计划_28', 12, 5, 9, 11, DATE_SUB(NOW(), INTERVAL -3 DAY), 1),
(1, 1, '日常补充巡检计划_29', 1, 4, 7, 7, DATE_SUB(NOW(), INTERVAL 6 DAY), 0);

INSERT INTO inspection_record (tenant_id, task_id, device_id, result, remark, create_time) VALUES
(1, 10, 5, 1, '设备运行正常', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 15, 2, 0, '发现轻微隐患，已上报', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(3, 16, 10, 0, '发现轻微隐患，已上报', DATE_SUB(NOW(), INTERVAL -3 DAY)),
(3, 18, 10, 1, '设备运行正常', DATE_SUB(NOW(), INTERVAL -4 DAY)),
(2, 19, 8, 0, '发现轻微隐患，已上报', DATE_SUB(NOW(), INTERVAL 0 DAY)),
(1, 20, 7, 1, '设备运行正常', DATE_SUB(NOW(), INTERVAL -2 DAY)),
(3, 22, 10, 1, '设备运行正常', DATE_SUB(NOW(), INTERVAL -3 DAY)),
(2, 23, 9, 0, '发现轻微隐患，已上报', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(3, 25, 10, 1, '设备运行正常', DATE_SUB(NOW(), INTERVAL -2 DAY)),
(1, 27, 3, 0, '发现轻微隐患，已上报', DATE_SUB(NOW(), INTERVAL -5 DAY)),
(3, 28, 10, 0, '发现轻微隐患，已上报', DATE_SUB(NOW(), INTERVAL -1 DAY)),
(2, 30, 9, 0, '发现轻微隐患，已上报', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 31, 10, 0, '发现轻微隐患，已上报', DATE_SUB(NOW(), INTERVAL -5 DAY)),
(3, 34, 10, 0, '发现轻微隐患，已上报', DATE_SUB(NOW(), INTERVAL 0 DAY)),
(3, 36, 10, 0, '发现轻微隐患，已上报', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 37, 9, 1, '设备运行正常', DATE_SUB(NOW(), INTERVAL -3 DAY));

-- ===================== 补充：30条公告及关联目标 =====================
INSERT INTO notice (tenant_id, title, content, publish_status, scheduled_time, create_time) VALUES
(1, '节假日物业服务安排 0', '补充生成的测试公告内容正文...', 0, DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY)),
(3, '节假日物业服务安排 1', '补充生成的测试公告内容正文...', 1, NULL, DATE_SUB(NOW(), INTERVAL 28 DAY)),
(2, '防范电信诈骗宣传 2', '补充生成的测试公告内容正文...', 1, NULL, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(1, '防范电信诈骗宣传 3', '补充生成的测试公告内容正文...', 0, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, '防范电信诈骗宣传 4', '补充生成的测试公告内容正文...', 1, NULL, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(3, '温馨提示：气温下降注意保暖 5', '补充生成的测试公告内容正文...', 1, NULL, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(2, '关于规范电动车停放的通知 6', '补充生成的测试公告内容正文...', 1, NULL, DATE_SUB(NOW(), INTERVAL 25 DAY)),
(1, '节假日物业服务安排 7', '补充生成的测试公告内容正文...', 0, DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY)),
(2, '车库清洗预告 8', '补充生成的测试公告内容正文...', 0, DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 27 DAY)),
(3, '小区设施维护进度通报 9', '补充生成的测试公告内容正文...', 1, NULL, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(1, '车库清洗预告 10', '补充生成的测试公告内容正文...', 0, DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, '关于规范电动车停放的通知 11', '补充生成的测试公告内容正文...', 0, DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, '温馨提示：气温下降注意保暖 12', '补充生成的测试公告内容正文...', 1, NULL, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(1, '车库清洗预告 13', '补充生成的测试公告内容正文...', 1, NULL, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(2, '车库清洗预告 14', '补充生成的测试公告内容正文...', 0, DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, '绿化修剪作业通知 15', '补充生成的测试公告内容正文...', 0, DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 22 DAY)),
(1, '关于规范电动车停放的通知 16', '补充生成的测试公告内容正文...', 0, DATE_ADD(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY)),
(1, '节假日物业服务安排 17', '补充生成的测试公告内容正文...', 0, DATE_ADD(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY)),
(2, '温馨提示：气温下降注意保暖 18', '补充生成的测试公告内容正文...', 0, DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 16 DAY)),
(3, '关于规范电动车停放的通知 19', '补充生成的测试公告内容正文...', 0, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY)),
(3, '节假日物业服务安排 20', '补充生成的测试公告内容正文...', 0, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 16 DAY)),
(2, '节假日物业服务安排 21', '补充生成的测试公告内容正文...', 0, DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 23 DAY)),
(3, '关于规范电动车停放的通知 22', '补充生成的测试公告内容正文...', 0, DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1, '温馨提示：气温下降注意保暖 23', '补充生成的测试公告内容正文...', 1, NULL, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, '小区设施维护进度通报 24', '补充生成的测试公告内容正文...', 1, NULL, DATE_SUB(NOW(), INTERVAL 24 DAY)),
(3, '小区设施维护进度通报 25', '补充生成的测试公告内容正文...', 1, NULL, DATE_SUB(NOW(), INTERVAL 21 DAY)),
(1, '关于规范电动车停放的通知 26', '补充生成的测试公告内容正文...', 0, DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY)),
(3, '绿化修剪作业通知 27', '补充生成的测试公告内容正文...', 1, NULL, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, '防范电信诈骗宣传 28', '补充生成的测试公告内容正文...', 1, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, '防范电信诈骗宣传 29', '补充生成的测试公告内容正文...', 1, NULL, DATE_SUB(NOW(), INTERVAL 6 DAY));

INSERT INTO notice_target (tenant_id, notice_id, building_id) VALUES
(1, 4, 3),
(3, 5, 23),
(2, 6, 12),
(1, 7, 3),
(2, 8, 14),
(2, 8, 13),
(3, 9, 21),
(3, 9, 21),
(3, 9, 18),
(2, 10, 16),
(2, 10, 12),
(1, 11, 4),
(1, 11, 2),
(2, 12, 14),
(3, 13, 21),
(1, 14, 2),
(1, 14, 5),
(1, 14, 7),
(3, 15, 23),
(3, 16, 21),
(3, 16, 19),
(1, 17, 3),
(2, 18, 10),
(2, 18, 16),
(2, 18, 15),
(3, 19, 17),
(3, 19, 19),
(3, 19, 18),
(1, 20, 2),
(1, 20, 2),
(1, 20, 3),
(1, 21, 5),
(1, 21, 5),
(2, 22, 11),
(2, 22, 9),
(3, 23, 23),
(3, 23, 21),
(3, 23, 17),
(3, 24, 23),
(2, 25, 11),
(2, 25, 15),
(3, 26, 24),
(3, 26, 22),
(1, 27, 2),
(1, 27, 2),
(3, 28, 24),
(3, 29, 22),
(1, 30, 2),
(1, 30, 6),
(3, 31, 22),
(3, 31, 22),
(3, 32, 24),
(3, 32, 23),
(3, 32, 22),
(1, 33, 4);

-- ===================== 补充：30条消息记录 =====================
INSERT INTO message (tenant_id, user_id, type, content, is_read, create_time) VALUES
(2, 135, 'SYSTEM', '您的工单/系统状态有更新，请查看详情。', 1, DATE_SUB(NOW(), INTERVAL 23 HOUR)),
(1, 5, 'NEW_ORDER', '您有新的任务，请及时处理。', 1, DATE_SUB(NOW(), INTERVAL 24 HOUR)),
(3, 12, 'NEW_ORDER', '您有新的任务，请及时处理。', 0, DATE_SUB(NOW(), INTERVAL 44 HOUR)),
(1, 119, 'ORDER_UPDATE', '您的工单/系统状态有更新，请查看详情。', 1, DATE_SUB(NOW(), INTERVAL 8 HOUR)),
(2, 140, 'SYSTEM', '您的工单/系统状态有更新，请查看详情。', 0, DATE_SUB(NOW(), INTERVAL 46 HOUR)),
(2, 152, 'NOTICE', '您的工单/系统状态有更新，请查看详情。', 1, DATE_SUB(NOW(), INTERVAL 25 HOUR)),
(1, 7, 'INSPECTION_REMINDER', '您有新的任务，请及时处理。', 1, DATE_SUB(NOW(), INTERVAL 46 HOUR)),
(1, 121, 'SYSTEM', '您的工单/系统状态有更新，请查看详情。', 1, DATE_SUB(NOW(), INTERVAL 36 HOUR)),
(3, 14, 'INSPECTION_REMINDER', '您有新的任务，请及时处理。', 1, DATE_SUB(NOW(), INTERVAL 14 HOUR)),
(2, 134, 'NOTICE', '您的工单/系统状态有更新，请查看详情。', 0, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(1, 4, 'INSPECTION_REMINDER', '您有新的任务，请及时处理。', 0, DATE_SUB(NOW(), INTERVAL 29 HOUR)),
(2, 11, 'NEW_ORDER', '您有新的任务，请及时处理。', 0, DATE_SUB(NOW(), INTERVAL 38 HOUR)),
(3, 169, 'ORDER_UPDATE', '您的工单/系统状态有更新，请查看详情。', 0, DATE_SUB(NOW(), INTERVAL 39 HOUR)),
(2, 11, 'INSPECTION_REMINDER', '您有新的任务，请及时处理。', 0, DATE_SUB(NOW(), INTERVAL 31 HOUR)),
(3, 159, 'NOTICE', '您的工单/系统状态有更新，请查看详情。', 0, DATE_SUB(NOW(), INTERVAL 36 HOUR)),
(1, 102, 'NOTICE', '您的工单/系统状态有更新，请查看详情。', 1, DATE_SUB(NOW(), INTERVAL 10 HOUR)),
(1, 105, 'ORDER_UPDATE', '您的工单/系统状态有更新，请查看详情。', 1, DATE_SUB(NOW(), INTERVAL 38 HOUR)),
(3, 160, 'SYSTEM', '您的工单/系统状态有更新，请查看详情。', 0, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(2, 9, 'INSPECTION_REMINDER', '您有新的任务，请及时处理。', 0, DATE_SUB(NOW(), INTERVAL 24 HOUR)),
(2, 8, 'NEW_ORDER', '您有新的任务，请及时处理。', 1, DATE_SUB(NOW(), INTERVAL 15 HOUR)),
(1, 124, 'ORDER_UPDATE', '您的工单/系统状态有更新，请查看详情。', 1, DATE_SUB(NOW(), INTERVAL 21 HOUR)),
(1, 118, 'SYSTEM', '您的工单/系统状态有更新，请查看详情。', 0, DATE_SUB(NOW(), INTERVAL 46 HOUR)),
(1, 7, 'INSPECTION_REMINDER', '您有新的任务，请及时处理。', 1, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(2, 9, 'NEW_ORDER', '您有新的任务，请及时处理。', 1, DATE_SUB(NOW(), INTERVAL 26 HOUR)),
(1, 7, 'INSPECTION_REMINDER', '您有新的任务，请及时处理。', 1, DATE_SUB(NOW(), INTERVAL 12 HOUR)),
(2, 8, 'INSPECTION_REMINDER', '您有新的任务，请及时处理。', 1, DATE_SUB(NOW(), INTERVAL 30 HOUR)),
(3, 14, 'NEW_ORDER', '您有新的任务，请及时处理。', 1, DATE_SUB(NOW(), INTERVAL 26 HOUR)),
(3, 172, 'NOTICE', '您的工单/系统状态有更新，请查看详情。', 0, DATE_SUB(NOW(), INTERVAL 10 HOUR)),
(2, 153, 'SYSTEM', '您的工单/系统状态有更新，请查看详情。', 1, DATE_SUB(NOW(), INTERVAL 15 HOUR)),
(3, 172, 'ORDER_UPDATE', '您的工单/系统状态有更新，请查看详情。', 0, DATE_SUB(NOW(), INTERVAL 2 HOUR));