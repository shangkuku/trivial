static private class ExcelExporter {
        static void exportExcel(HttpServletRequest request, HttpServletResponse response, Workbook workbook)
                throws IOException {
            String fileName = CommonUtils.encodeFileName("学生成绩.xlsx", request.getHeader("User-Agent"));
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("content-disposition", "attachment;filename=" + fileName);
            try (OutputStream os = response.getOutputStream()) {
                workbook.write(os);
            }
        }

        static Workbook createWorkbook(Collection<Map<String, Object>> items, Collection<Column> columns) {
            Workbook workbook = new XSSFWorkbook();
            Table table = new Table(workbook.createSheet(), items, columns);
            table.render();
            return workbook;
        }

        static Column createColumn(String name, String title, RenderFormatter ro) {
            Renderer renderer;
            if (ro == RenderFormatter.DOUBLE) {
                renderer = new DoubleRender();
            } else {
                renderer = new Renderer();
            }
            return new Column(name, title, renderer);
        }

        static Column createColumn(String name, String title) {
            return createColumn(name, title, null);
        }

        static class Table {
            Sheet sheet;
            Collection<Map<String, Object>> items;
            Collection<Column> columns;
            //当前row
            private int rowIndex = 0;

            public Workbook getWorkbook() {
                return this.sheet.getWorkbook();
            }

            public Table(Sheet sheet, Collection<Map<String, Object>> items, Collection<Column> columns) {
                this.sheet = sheet;
                this.items = items;
                this.columns = columns;
            }

            public void render() {
                createHeader();
                createBody();
            }

            private void createBody() {
                if (CollectionUtils.isEmpty(this.columns)) {
                    return;
                }
                for (Map<String, Object> item : this.items) {
                    Row row = sheet.createRow(this.rowIndex++);
//                CellStyle bodyStyle = rowNum % 2 == 0 ? oddStyle : evenStyle;
                    int columnIndex = 0;
                    for (Column col : this.columns) {
                        String key = col.getName();
                        Object value = item.get(key);
                        Renderer renderer = col.getRenderer();
                        if (renderer != null) {
                            value = renderer.render(col, value, item);
                        }
                        createBodyCell(row, null, columnIndex++, value);
                    }
                }
            }

            private <V> Cell createBodyCell(Row row, CellStyle style, int colIdx, V value) {
                Cell cell = row.createCell(colIdx);
                //暂时都当string来处理
                cell.setCellValue(value == null ? null : value.toString());
                cell.setCellStyle(style);
                return cell;
            }

            private void createHeader() {
                if (CollectionUtils.isEmpty(columns)) {
                    return;
                }
                Row row = sheet.createRow(0);
                row.setHeightInPoints(32);
                CellStyle headerStyle = getHeaderStyle(sheet.getWorkbook());
                int columnIndex = 0;
                for (Column col : columns) {
                    createHeaderCell(row, headerStyle, columnIndex++, col.getTitle());
                }
                this.rowIndex++;
            }

            private Cell createHeaderCell(Row row, CellStyle style, int colIdx, String value) {
                Cell cell = row.createCell(colIdx);
                cell.setCellValue(value);
                cell.setCellStyle(style);
                return cell;
            }

            private CellStyle getHeaderStyle(Workbook workbook) {
                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setFontHeightInPoints((short) 12);
                headerStyle.setFont(font);
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setAlignment(HorizontalAlignment.CENTER);
                headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                return headerStyle;
            }
        }

        static private class Column {
            private String name;
            private String title;
            private Renderer renderer;

            public Column(String name, String title, Renderer renderer) {
                this.name = name;
                this.title = title;
                this.renderer = renderer == null ? new Renderer() : renderer;
            }

            public Column(String name, String title) {
                this.name = name;
                this.title = title;
                this.renderer = new Renderer();
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public Renderer getRenderer() {
                return renderer;
            }

            public void setRenderer(Renderer renderer) {
                this.renderer = renderer;
            }
        }

        static class Renderer {
            protected Object render(Column col, Object v, Map<String, Object> data) {
                return v == null ? "-" : v.toString();
            }
        }

        static class DoubleRender extends Renderer {
            protected Object render(Column col, Object v, Map<String, Object> data) {
                Object value = v;
                if (value == null) {
                    return "-";
                }
                try {
                    Double d = Double.parseDouble(value.toString());
                    BigDecimal b = new BigDecimal(d);
                    return b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                } catch (NumberFormatException e) {
                    return value;
                }
            }
        }

        enum RenderFormatter {
            DEFAULT,DOUBLE
        }
    }
