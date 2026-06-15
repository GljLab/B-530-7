package com.example.permission.service;

import com.example.permission.entity.MaintenanceOrder;
import com.example.permission.entity.Room;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExportService {

    @Autowired
    private RoomService roomService;

    private static final Map<Integer, String> STATUS_MAP = new HashMap<>();
    static {
        STATUS_MAP.put(1, "空闲");
        STATUS_MAP.put(2, "已预订");
        STATUS_MAP.put(3, "已入住");
        STATUS_MAP.put(4, "待清洁");
        STATUS_MAP.put(5, "清洁中");
        STATUS_MAP.put(6, "维修中");
        STATUS_MAP.put(7, "停用");
    }

    public byte[] exportRooms(List<Room> rooms, List<String> exportFields,
                               String operatorName, String filterDesc) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("房间数据");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle infoStyle = createInfoStyle(workbook);

            int rowIdx = 0;

            Row infoRow1 = sheet.createRow(rowIdx++);
            Cell cell1 = infoRow1.createCell(0);
            cell1.setCellValue("导出时间：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            cell1.setCellStyle(infoStyle);

            Row infoRow2 = sheet.createRow(rowIdx++);
            Cell cell2 = infoRow2.createCell(0);
            cell2.setCellValue("导出人：" + operatorName);
            cell2.setCellStyle(infoStyle);

            Row infoRow3 = sheet.createRow(rowIdx++);
            Cell cell3 = infoRow3.createCell(0);
            cell3.setCellValue("筛选条件：" + (filterDesc != null ? filterDesc : "全部房间"));
            cell3.setCellStyle(infoStyle);

            rowIdx++;

            Row headerRow = sheet.createRow(rowIdx++);
            int colIdx = 0;

            Map<String, Integer> fieldIndex = new HashMap<>();

            if (exportFields.contains("roomNumber")) {
                Cell c = headerRow.createCell(colIdx);
                c.setCellValue("房间号");
                c.setCellStyle(headerStyle);
                fieldIndex.put("roomNumber", colIdx++);
            }
            if (exportFields.contains("building")) {
                Cell c = headerRow.createCell(colIdx);
                c.setCellValue("楼栋");
                c.setCellStyle(headerStyle);
                fieldIndex.put("building", colIdx++);
            }
            if (exportFields.contains("floor")) {
                Cell c = headerRow.createCell(colIdx);
                c.setCellValue("楼层");
                c.setCellStyle(headerStyle);
                fieldIndex.put("floor", colIdx++);
            }
            if (exportFields.contains("roomType")) {
                Cell c = headerRow.createCell(colIdx);
                c.setCellValue("房型");
                c.setCellStyle(headerStyle);
                fieldIndex.put("roomType", colIdx++);
            }
            if (exportFields.contains("orientation")) {
                Cell c = headerRow.createCell(colIdx);
                c.setCellValue("朝向");
                c.setCellStyle(headerStyle);
                fieldIndex.put("orientation", colIdx++);
            }
            if (exportFields.contains("viewType")) {
                Cell c = headerRow.createCell(colIdx);
                c.setCellValue("景观");
                c.setCellStyle(headerStyle);
                fieldIndex.put("viewType", colIdx++);
            }
            if (exportFields.contains("locationFeatures")) {
                Cell c = headerRow.createCell(colIdx);
                c.setCellValue("位置特点");
                c.setCellStyle(headerStyle);
                fieldIndex.put("locationFeatures", colIdx++);
            }
            if (exportFields.contains("specialTags")) {
                Cell c = headerRow.createCell(colIdx);
                c.setCellValue("特殊标识");
                c.setCellStyle(headerStyle);
                fieldIndex.put("specialTags", colIdx++);
            }
            if (exportFields.contains("status")) {
                Cell c = headerRow.createCell(colIdx);
                c.setCellValue("当前状态");
                c.setCellStyle(headerStyle);
                fieldIndex.put("status", colIdx++);
            }
            if (exportFields.contains("basePrice")) {
                Cell c = headerRow.createCell(colIdx);
                c.setCellValue("房型基础价格");
                c.setCellStyle(headerStyle);
                fieldIndex.put("basePrice", colIdx++);
            }

            for (Room room : rooms) {
                Row dataRow = sheet.createRow(rowIdx++);
                if (fieldIndex.containsKey("roomNumber")) {
                    dataRow.createCell(fieldIndex.get("roomNumber")).setCellValue(room.getRoomNumber() != null ? room.getRoomNumber() : "");
                }
                if (fieldIndex.containsKey("building")) {
                    dataRow.createCell(fieldIndex.get("building")).setCellValue(room.getBuildingName() != null ? room.getBuildingName() : "");
                }
                if (fieldIndex.containsKey("floor")) {
                    dataRow.createCell(fieldIndex.get("floor")).setCellValue(room.getFloorName() != null ? room.getFloorName() : (room.getFloorNumber() != null ? room.getFloorNumber() + "层" : ""));
                }
                if (fieldIndex.containsKey("roomType")) {
                    dataRow.createCell(fieldIndex.get("roomType")).setCellValue(room.getRoomTypeName() != null ? room.getRoomTypeName() : "");
                }
                if (fieldIndex.containsKey("orientation")) {
                    dataRow.createCell(fieldIndex.get("orientation")).setCellValue(room.getOrientation() != null ? room.getOrientation() : "");
                }
                if (fieldIndex.containsKey("viewType")) {
                    dataRow.createCell(fieldIndex.get("viewType")).setCellValue(room.getViewType() != null ? room.getViewType() : "");
                }
                if (fieldIndex.containsKey("locationFeatures")) {
                    dataRow.createCell(fieldIndex.get("locationFeatures")).setCellValue(room.getLocationFeatures() != null ? room.getLocationFeatures() : "");
                }
                if (fieldIndex.containsKey("specialTags")) {
                    dataRow.createCell(fieldIndex.get("specialTags")).setCellValue(room.getSpecialTags() != null ? room.getSpecialTags() : "");
                }
                if (fieldIndex.containsKey("status")) {
                    String statusStr = room.getStatus() != null ? (STATUS_MAP.getOrDefault(room.getStatus(), "未知") + "(" + room.getStatus() + ")") : "";
                    dataRow.createCell(fieldIndex.get("status")).setCellValue(statusStr);
                }
                if (fieldIndex.containsKey("basePrice")) {
                    if (room.getRoomType() != null && room.getRoomType().getBasePrice() != null) {
                        dataRow.createCell(fieldIndex.get("basePrice")).setCellValue(room.getRoomType().getBasePrice().doubleValue());
                    } else {
                        dataRow.createCell(fieldIndex.get("basePrice")).setCellValue("");
                    }
                }
            }

            for (int i = 0; i < colIdx; i++) {
                sheet.autoSizeColumn(i);
                int width = sheet.getColumnWidth(i);
                if (width < 3000) width = 3000;
                if (width > 15000) width = 15000;
                sheet.setColumnWidth(i, width);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败：" + e.getMessage(), e);
        }
    }

    public String generateFileName() {
        return "房间数据_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    public byte[] exportMaintenanceOrders(List<MaintenanceOrder> orders, String operatorName) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("维护单数据");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle infoStyle = createInfoStyle(workbook);

            int rowIdx = 0;
            Row infoRow1 = sheet.createRow(rowIdx++);
            infoRow1.createCell(0).setCellValue("导出时间：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            infoRow1.getCell(0).setCellStyle(infoStyle);
            Row infoRow2 = sheet.createRow(rowIdx++);
            infoRow2.createCell(0).setCellValue("导出人：" + operatorName);
            infoRow2.getCell(0).setCellStyle(infoStyle);
            rowIdx++;

            Row headerRow = sheet.createRow(rowIdx++);
            String[] headers = {"维护单号", "房间号", "维护类型", "优先级", "状态", "创建人", "创建时间",
                    "分配人员", "接单时间", "实际用时(小时)", "维修费用(元)", "完成时间", "验收人", "验收结果"};
            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            for (MaintenanceOrder order : orders) {
                Row dataRow = sheet.createRow(rowIdx++);
                int col = 0;
                dataRow.createCell(col++).setCellValue(order.getOrderNo() != null ? order.getOrderNo() : "");
                dataRow.createCell(col++).setCellValue(order.getRoomNumber() != null ? order.getRoomNumber() : "");
                dataRow.createCell(col++).setCellValue(MaintenanceOrderService.getTypeText(order.getMaintenanceType()));
                dataRow.createCell(col++).setCellValue(MaintenanceOrderService.getPriorityText(order.getPriority()));
                dataRow.createCell(col++).setCellValue(MaintenanceOrderService.getStatusText(order.getStatus()));
                dataRow.createCell(col++).setCellValue(order.getCreateUserName() != null ? order.getCreateUserName() : "");
                dataRow.createCell(col++).setCellValue(order.getCreateTime() != null ? order.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
                dataRow.createCell(col++).setCellValue(order.getAssignedUserName() != null ? order.getAssignedUserName() : "");
                dataRow.createCell(col++).setCellValue(order.getAcceptTime() != null ? order.getAcceptTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
                dataRow.createCell(col++).setCellValue(order.getActualHours() != null ? order.getActualHours().doubleValue() : 0);
                dataRow.createCell(col++).setCellValue(order.getMaintenanceCost() != null ? order.getMaintenanceCost().doubleValue() : 0);
                dataRow.createCell(col++).setCellValue(order.getFinishTime() != null ? order.getFinishTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
                dataRow.createCell(col++).setCellValue(order.getInspectorName() != null ? order.getInspectorName() : "");
                String inspectResult = "";
                if (order.getInspectResult() != null) {
                    inspectResult = order.getInspectResult() == 1 ? "通过" : "不通过";
                }
                dataRow.createCell(col).setCellValue(inspectResult);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int width = sheet.getColumnWidth(i);
                if (width < 3000) width = 3000;
                if (width > 15000) width = 15000;
                sheet.setColumnWidth(i, width);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出维护单Excel失败：" + e.getMessage(), e);
        }
    }

    public byte[] exportMaintenanceStatistics(Map<String, Object> overview,
                                                List<Map<String, Object>> topRooms,
                                                Map<String, Object> typeDist,
                                                List<Map<String, Object>> costTrend,
                                                Map<String, Object> durationStats,
                                                List<Map<String, Object>> staffWorkload,
                                                String operatorName) {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle infoStyle = createInfoStyle(workbook);

            int rowIdx;
            Row infoRow;

            Sheet sheet1 = workbook.createSheet("总体概览");
            rowIdx = 0;
            infoRow = sheet1.createRow(rowIdx++);
            infoRow.createCell(0).setCellValue("导出时间：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            infoRow.getCell(0).setCellStyle(infoStyle);
            infoRow = sheet1.createRow(rowIdx++);
            infoRow.createCell(0).setCellValue("导出人：" + operatorName);
            infoRow.getCell(0).setCellStyle(infoStyle);
            rowIdx++;
            Row h1 = sheet1.createRow(rowIdx++);
            h1.createCell(0).setCellValue("指标"); h1.getCell(0).setCellStyle(headerStyle);
            h1.createCell(1).setCellValue("数值"); h1.getCell(1).setCellStyle(headerStyle);
            String[][] overviewData = {
                    {"维护单总数", safeString(overview.get("totalOrders"))},
                    {"待分配数", safeString(overview.get("pendingCount"))},
                    {"处理中数", safeString(overview.get("processingCount"))},
                    {"本月维护单数", safeString(overview.get("monthOrderCount"))},
                    {"本月费用(元)", safeString(overview.get("monthCost"))},
                    {"平均处理时长(小时)", safeString(overview.get("avgHours"))}
            };
            for (String[] row : overviewData) {
                Row r = sheet1.createRow(rowIdx++);
                r.createCell(0).setCellValue(row[0]);
                r.createCell(1).setCellValue(row[1]);
            }
            sheet1.autoSizeColumn(0); sheet1.autoSizeColumn(1);

            Sheet sheet2 = workbook.createSheet("维修频率TOP10");
            rowIdx = 0;
            Row h2 = sheet2.createRow(rowIdx++);
            String[] topHeaders = {"排名", "房间号", "维护次数", "累计费用(元)"};
            for (int i = 0; i < topHeaders.length; i++) {
                h2.createCell(i).setCellValue(topHeaders[i]);
                h2.getCell(i).setCellStyle(headerStyle);
            }
            int rank = 1;
            for (Map<String, Object> item : topRooms) {
                Row r = sheet2.createRow(rowIdx++);
                r.createCell(0).setCellValue(rank++);
                r.createCell(1).setCellValue(item.get("roomNumber") != null ? item.get("roomNumber").toString() : "");
                r.createCell(2).setCellValue(safeInt(item.get("maintenanceCount")));
                r.createCell(3).setCellValue(safeDouble(item.get("totalCost")));
            }
            for (int i = 0; i < topHeaders.length; i++) sheet2.autoSizeColumn(i);

            Sheet sheet3 = workbook.createSheet("维护类型分布");
            rowIdx = 0;
            Row h3 = sheet3.createRow(rowIdx++);
            h3.createCell(0).setCellValue("维护类型"); h3.getCell(0).setCellStyle(headerStyle);
            h3.createCell(1).setCellValue("数量"); h3.getCell(1).setCellStyle(headerStyle);
            h3.createCell(2).setCellValue("占比"); h3.getCell(2).setCellStyle(headerStyle);
            List<Map<String, Object>> typeList = typeDist != null && typeDist.get("list") != null
                    ? (List<Map<String, Object>>) typeDist.get("list") : new ArrayList<>();
            int total = safeInt(typeDist != null ? typeDist.get("total") : null);
            if (total <= 0) total = 1;
            for (Map<String, Object> item : typeList) {
                Row r = sheet3.createRow(rowIdx++);
                int count = safeInt(item.get("count"));
                r.createCell(0).setCellValue(item.get("typeName") != null ? item.get("typeName").toString() : "");
                r.createCell(1).setCellValue(count);
                r.createCell(2).setCellValue(String.format("%.2f%%", total > 0 ? count * 100.0 / total : 0));
            }
            for (int i = 0; i < 3; i++) sheet3.autoSizeColumn(i);

            Sheet sheet4 = workbook.createSheet("费用趋势(近6个月)");
            rowIdx = 0;
            Row h4 = sheet4.createRow(rowIdx++);
            h4.createCell(0).setCellValue("月份"); h4.getCell(0).setCellStyle(headerStyle);
            h4.createCell(1).setCellValue("费用(元)"); h4.getCell(1).setCellStyle(headerStyle);
            h4.createCell(2).setCellValue("维护单数"); h4.getCell(2).setCellStyle(headerStyle);
            for (Map<String, Object> item : costTrend) {
                Row r = sheet4.createRow(rowIdx++);
                r.createCell(0).setCellValue(item.get("month") != null ? item.get("month").toString() : "");
                r.createCell(1).setCellValue(safeDouble(item.get("cost")));
                r.createCell(2).setCellValue(safeInt(item.get("count")));
            }
            for (int i = 0; i < 3; i++) sheet4.autoSizeColumn(i);

            Sheet sheet5 = workbook.createSheet("维修时长统计");
            rowIdx = 0;
            Row h5 = sheet5.createRow(rowIdx++);
            h5.createCell(0).setCellValue("指标"); h5.getCell(0).setCellStyle(headerStyle);
            h5.createCell(1).setCellValue("数值"); h5.getCell(1).setCellStyle(headerStyle);
            String[][] durationData = {
                    {"平均处理时长(分钟)", safeString(durationStats.get("avgDurationMinutes"))},
                    {"最长处理时长(分钟)", safeString(durationStats.get("maxDurationMinutes"))},
                    {"超时单数", safeString(durationStats.get("timeoutCount"))},
                    {"超时率", String.format("%.2f%%", safeDouble(durationStats.get("timeoutRate")) * 100)}
            };
            for (String[] row : durationData) {
                Row r = sheet5.createRow(rowIdx++);
                r.createCell(0).setCellValue(row[0]);
                r.createCell(1).setCellValue(row[1]);
            }
            sheet5.autoSizeColumn(0); sheet5.autoSizeColumn(1);

            Sheet sheet6 = workbook.createSheet("人员工作量统计");
            rowIdx = 0;
            Row h6 = sheet6.createRow(rowIdx++);
            String[] staffHeaders = {"用户名", "姓名", "总工单数", "已完成", "平均工时(小时)", "验收通过率"};
            for (int i = 0; i < staffHeaders.length; i++) {
                h6.createCell(i).setCellValue(staffHeaders[i]);
                h6.getCell(i).setCellStyle(headerStyle);
            }
            for (Map<String, Object> item : staffWorkload) {
                Row r = sheet6.createRow(rowIdx++);
                r.createCell(0).setCellValue(item.get("username") != null ? item.get("username").toString() : "");
                r.createCell(1).setCellValue(item.get("nickname") != null ? item.get("nickname").toString() : "");
                r.createCell(2).setCellValue(safeInt(item.get("totalCount")));
                r.createCell(3).setCellValue(safeInt(item.get("finishedCount")));
                r.createCell(4).setCellValue(safeDouble(item.get("avgHours")));
                r.createCell(5).setCellValue(String.format("%.2f%%", safeDouble(item.get("passRate")) * 100));
            }
            for (int i = 0; i < staffHeaders.length; i++) sheet6.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出统计报表Excel失败：" + e.getMessage(), e);
        }
    }

    private CellStyle createInfoStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFont(font);
        return style;
    }

    private String safeString(Object obj) {
        if (obj == null) return "0";
        return obj.toString();
    }

    private double safeDouble(Object obj) {
        if (obj == null) return 0.0;
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int safeInt(Object obj) {
        if (obj == null) return 0;
        try {
            return Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public byte[] exportStatistics(Map<String, Object> params, String operatorName, String filterDesc) {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle infoStyle = createInfoStyle(workbook);

            int rowIdx;
            Row infoRow;

            Sheet sheet1 = workbook.createSheet("导出信息");
            rowIdx = 0;
            infoRow = sheet1.createRow(rowIdx++);
            infoRow.createCell(0).setCellValue("导出时间：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            infoRow.getCell(0).setCellStyle(infoStyle);
            infoRow = sheet1.createRow(rowIdx++);
            infoRow.createCell(0).setCellValue("导出人：" + operatorName);
            infoRow.getCell(0).setCellStyle(infoStyle);
            infoRow = sheet1.createRow(rowIdx++);
            infoRow.createCell(0).setCellValue("筛选条件：" + (filterDesc != null ? filterDesc : "全部数据"));
            infoRow.getCell(0).setCellStyle(infoStyle);
            sheet1.autoSizeColumn(0);

            if (params != null && params.get("floorUsageStats") != null) {
                Sheet sheet2 = workbook.createSheet("楼层使用率统计");
                rowIdx = 0;
                Row h2 = sheet2.createRow(rowIdx++);
                String[] floorHeaders = {"楼栋", "楼层号", "楼层名称", "房间总数", "空闲", "已入住", "维修中", "使用率(%)"};
                for (int i = 0; i < floorHeaders.length; i++) {
                    h2.createCell(i).setCellValue(floorHeaders[i]);
                    h2.getCell(i).setCellStyle(headerStyle);
                }
                List<Map<String, Object>> floorList = (List<Map<String, Object>>) params.get("floorUsageStats");
                for (Map<String, Object> item : floorList) {
                    Row r = sheet2.createRow(rowIdx++);
                    r.createCell(0).setCellValue(safeString(item.get("buildingName")));
                    r.createCell(1).setCellValue(safeInt(item.get("floorNumber")));
                    r.createCell(2).setCellValue(safeString(item.get("floorName")));
                    r.createCell(3).setCellValue(safeInt(item.get("total")));
                    r.createCell(4).setCellValue(safeInt(item.get("free")));
                    r.createCell(5).setCellValue(safeInt(item.get("occupied")));
                    r.createCell(6).setCellValue(safeInt(item.get("maintenance")));
                    r.createCell(7).setCellValue(safeDouble(item.get("usageRate")));
                }
                for (int i = 0; i < floorHeaders.length; i++) sheet2.autoSizeColumn(i);
            }

            if (params != null && params.get("roomTypeHeatStats") != null) {
                Sheet sheet3 = workbook.createSheet("房型热度分析");
                rowIdx = 0;
                Row h3 = sheet3.createRow(rowIdx++);
                String[] typeHeaders = {"房型名称", "房间总数", "空闲数", "空闲率(%)"};
                for (int i = 0; i < typeHeaders.length; i++) {
                    h3.createCell(i).setCellValue(typeHeaders[i]);
                    h3.getCell(i).setCellStyle(headerStyle);
                }
                Map<String, Object> roomTypeHeat = (Map<String, Object>) params.get("roomTypeHeatStats");
                List<Map<String, Object>> typeList = roomTypeHeat != null && roomTypeHeat.get("list") != null
                        ? (List<Map<String, Object>>) roomTypeHeat.get("list") : new ArrayList<>();
                for (Map<String, Object> item : typeList) {
                    Row r = sheet3.createRow(rowIdx++);
                    r.createCell(0).setCellValue(safeString(item.get("typeName")));
                    r.createCell(1).setCellValue(safeInt(item.get("total")));
                    r.createCell(2).setCellValue(safeInt(item.get("free")));
                    r.createCell(3).setCellValue(safeDouble(item.get("freeRate")));
                }
                for (int i = 0; i < typeHeaders.length; i++) sheet3.autoSizeColumn(i);
            }

            if (params != null && params.get("statusDurationStats") != null) {
                Sheet sheet4 = workbook.createSheet("状态时长统计");
                rowIdx = 0;
                Row h4 = sheet4.createRow(rowIdx++);
                h4.createCell(0).setCellValue("指标");
                h4.getCell(0).setCellStyle(headerStyle);
                h4.createCell(1).setCellValue("数值");
                h4.getCell(1).setCellStyle(headerStyle);

                Map<String, Object> statusDuration = (Map<String, Object>) params.get("statusDurationStats");
                String[][] durationData = {
                        {"平均清洁时长(小时)", safeString(statusDuration.get("avgCleanHours"))},
                        {"平均维修时长(天)", safeString(statusDuration.get("avgRepairDays"))},
                        {"空闲房间总数", safeString(statusDuration.get("totalFree"))}
                };
                for (String[] row : durationData) {
                    Row r = sheet4.createRow(rowIdx++);
                    r.createCell(0).setCellValue(row[0]);
                    r.createCell(1).setCellValue(row[1]);
                }
                sheet4.autoSizeColumn(0);
                sheet4.autoSizeColumn(1);

                rowIdx++;
                Row distHeader = sheet4.createRow(rowIdx++);
                distHeader.createCell(0).setCellValue("空闲时长分布");
                distHeader.getCell(0).setCellStyle(headerStyle);
                distHeader.createCell(1).setCellValue("房间数");
                distHeader.getCell(1).setCellStyle(headerStyle);

                List<Map<String, Object>> idleDist = statusDuration != null && statusDuration.get("idleDurationDist") != null
                        ? (List<Map<String, Object>>) statusDuration.get("idleDurationDist") : new ArrayList<>();
                for (Map<String, Object> item : idleDist) {
                    Row r = sheet4.createRow(rowIdx++);
                    r.createCell(0).setCellValue(safeString(item.get("range")));
                    r.createCell(1).setCellValue(safeInt(item.get("count")));
                }

                List<Map<String, Object>> longIdleRooms = statusDuration != null && statusDuration.get("longIdleRooms") != null
                        ? (List<Map<String, Object>>) statusDuration.get("longIdleRooms") : new ArrayList<>();
                if (!longIdleRooms.isEmpty()) {
                    rowIdx++;
                    Row longHeader = sheet4.createRow(rowIdx++);
                    longHeader.createCell(0).setCellValue("长期空闲房间(超过15天)");
                    longHeader.getCell(0).setCellStyle(headerStyle);
                    longHeader.createCell(1).setCellValue("空闲天数");
                    longHeader.getCell(1).setCellStyle(headerStyle);
                    for (Map<String, Object> item : longIdleRooms) {
                        Row r = sheet4.createRow(rowIdx++);
                        r.createCell(0).setCellValue(safeString(item.get("roomNumber")));
                        r.createCell(1).setCellValue(safeInt(item.get("idleDays")));
                    }
                }
            }

            if (params != null && params.get("statusTrend") != null) {
                Sheet sheet5 = workbook.createSheet("状态趋势数据");
                List<Map<String, Object>> trendList = (List<Map<String, Object>>) params.get("statusTrend");
                if (!trendList.isEmpty()) {
                    rowIdx = 0;
                    Row h5 = sheet5.createRow(rowIdx++);
                    Map<String, Object> firstItem = trendList.get(0);
                    List<String> keys = new ArrayList<>();
                    int colIdx = 0;
                    for (String key : firstItem.keySet()) {
                        if (!key.startsWith("_")) {
                            Cell c = h5.createCell(colIdx);
                            c.setCellValue(key.equals("date") ? "日期" : key);
                            c.setCellStyle(headerStyle);
                            keys.add(key);
                            colIdx++;
                        }
                    }
                    for (Map<String, Object> item : trendList) {
                        Row r = sheet5.createRow(rowIdx++);
                        for (int i = 0; i < keys.size(); i++) {
                            Object val = item.get(keys.get(i));
                            if (val instanceof Number) {
                                r.createCell(i).setCellValue(((Number) val).doubleValue());
                            } else {
                                r.createCell(i).setCellValue(safeString(val));
                            }
                        }
                    }
                    for (int i = 0; i < keys.size(); i++) sheet5.autoSizeColumn(i);
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出统计报表Excel失败：" + e.getMessage(), e);
        }
    }

    public String generateStatisticsFileName() {
        return "酒店统计报表_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
    }
}
