package burp.ui;

import burp.*;
import burp.ui.datmodel.ApiDataModel;
import burp.ui.renderer.HavingImportantRenderer;
import burp.ui.renderer.IsJsFindUrlRenderer;
import burp.util.Constants;
import burp.util.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class MailPanel extends JPanel implements IMessageEditorController {
    private String tagName;
    private JSplitPane mainSplitPane;
    private JSplitPane infoSplitPane;
    private static IMessageEditor requestTextEditor;
    private static IMessageEditor responseTextEditor;
    private static IHttpRequestResponse currentlyDisplayedItem;
    private JScrollPane upScrollPane;
    private ConfigPanel configPanel;
    public static ITextEditor resultDeViewer;
    private static DefaultTableModel model;
    public static JTable table;
    public static int selectRow = 0;

    public static String historySearchText = "";
    public static String historySearchType = null;

    public MailPanel(IBurpExtenderCallbacks callbacks, String name) {
        // 主分隔面板
        mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        infoSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        setLayout(new BorderLayout());
        tagName = name;

        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BorderLayout());

        // 首行配置面板
        configPanel = new ConfigPanel();

        // 数据展示面板
        model = new DefaultTableModel(new Object[]{"#", "ID", "URl", "PATH Number", "Method", "status", "isJsFindUrl", "HavingImportant", "Result", "describe", "Time"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // This will make all cells of the table non-editable
                return false;
            }
        };
        table = new JTable(model){
            // 重写getToolTipText方法以返回特定单元格的数据
            public String getToolTipText(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
                if (row > -1 && col > -1) {
                    Object value = getValueAt(row, col);
                    return value == null ? null : value.toString();
                }
                return super.getToolTipText(e);
            }
        };;
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        upScrollPane = new JScrollPane(table);
        // 将upScrollPane作为mainSplitPane的上半部分
        mainSplitPane.setTopComponent(upScrollPane);

        // 前两列设置宽度 30px、60px
        table.getColumnModel().getColumn(0).setMaxWidth(30);
        table.getColumnModel().getColumn(1).setMaxWidth(60);
        table.getColumnModel().getColumn(2).setMinWidth(300);
        table.getColumnModel().getColumn(7).setMinWidth(60);
        table.getColumnModel().getColumn(8).setMinWidth(150);
        table.getColumnModel().getColumn(10).setMinWidth(180);

        // 创建一个居中对齐的单元格渲染器
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);

        table.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(7).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(8).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(9).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(10).setCellRenderer(leftRenderer);

        IsJsFindUrlRenderer isJsFindUrlRenderer = new IsJsFindUrlRenderer();
        table.getColumnModel().getColumn(6).setCellRenderer(isJsFindUrlRenderer);
        HavingImportantRenderer havingImportantRenderer = new HavingImportantRenderer();
        table.getColumnModel().getColumn(7).setCellRenderer(havingImportantRenderer);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        int row = table.rowAtPoint(e.getPoint());
                        if (row >= 0) {
                            selectRow = row;
                            String listStatus = (String)table.getModel().getValueAt(row, 0);
                            String url;
                            if (listStatus.equals(Constants.TREE_STATUS_COLLAPSE) || listStatus.equals(Constants.TREE_STATUS_EXPAND)){
                                url = (String)table.getModel().getValueAt(row, 2);
                                ApiDataModel apiDataModel = IProxyScanner.apiDataModelMap.get(url);
                                requestTextEditor.setMessage(apiDataModel.getRequestResponse().getRequest(), true);
                                responseTextEditor.setMessage(apiDataModel.getRequestResponse().getResponse(), false);
                                resultDeViewer.setText((apiDataModel.getResultInfo()).getBytes());
                                currentlyDisplayedItem = apiDataModel.getRequestResponse();
                                if (apiDataModel.getListStatus().equals(Constants.TREE_STATUS_COLLAPSE)){
                                    apiDataModel.setListStatus(Constants.TREE_STATUS_EXPAND);
                                    modelExpand(apiDataModel, row);
                                } else if (apiDataModel.getListStatus().equals(Constants.TREE_STATUS_EXPAND)) {
                                    apiDataModel.setListStatus(Constants.TREE_STATUS_COLLAPSE);
                                    modeCollapse(apiDataModel, row);
                                }
                            }else{
                                String path = (String)table.getModel().getValueAt(row, 2);
                                url = findUrlFromPath(row);
                                ApiDataModel apiDataModel = IProxyScanner.apiDataModelMap.get(url);
                                Map<String, Object> pathData = apiDataModel.getPathData();
                                Map<String, Object> matchPathData = (Map<String, Object>)pathData.get(path);
                                requestTextEditor.setMessage(((IHttpRequestResponse)matchPathData.get("responseRequest")).getRequest(), true);
                                responseTextEditor.setMessage(((IHttpRequestResponse)matchPathData.get("responseRequest")).getResponse(), false);
                                resultDeViewer.setText(((String)matchPathData.get("result info")).getBytes());
                                currentlyDisplayedItem = ((IHttpRequestResponse)matchPathData.get("responseRequest"));
                            }
                        }
                    }
                });

            }
        });

        // 请求的面板
        requestTextEditor = callbacks.createMessageEditor(this, false);

        // 响应的面板
        responseTextEditor = callbacks.createMessageEditor(this, false);

        // 详细结果面板
        resultDeViewer = BurpExtender.getCallbacks().createTextEditor();

        toolbar.add(configPanel, BorderLayout.NORTH);
        toolbar.add(mainSplitPane, BorderLayout.CENTER);
        add(toolbar, BorderLayout.NORTH);
        add(infoSplitPane, BorderLayout.CENTER);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Result Info", resultDeViewer.getComponent());
        tabs.addTab("Original Response", responseTextEditor.getComponent());
        tabs.addTab("Request", requestTextEditor.getComponent());
        infoSplitPane.setBottomComponent(tabs);

        // 构建一个定时刷新页面函数
        // 创建一个每5秒触发一次的定时器
        int delay = 5000; // 延迟时间，单位为毫秒
        Timer timer = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 调用刷新表格的方法
                try{
                    refreshTableModel();
                } catch (Exception ep){
                    ep.printStackTrace(BurpExtender.getStdout());
                }
            }
        });

        // 启动定时器
        timer.start();
    }

    public static void refreshTableModel(){
        // 刷新页面, 如果自动更新关闭，则不刷新页面内容
        if(ConfigPanel.getFlashButtonStatus()){
            return;
        }
        // 触发显示所有行事件
        String searchText = "";
        if (!ConfigPanel.searchField.getText().isEmpty()){
            searchText = ConfigPanel.searchField.getText();
        }

        String selectedOption = (String)ConfigPanel.choicesComboBox.getSelectedItem();
        MailPanel.showFilter(selectedOption, searchText);

    }

    @Override
    public byte[] getRequest() {
        return currentlyDisplayedItem.getRequest();
    }

    @Override
    public byte[] getResponse() {
        return currentlyDisplayedItem.getResponse();
    }

    @Override
    public IHttpService getHttpService() {
        return currentlyDisplayedItem.getHttpService();
    }

    public static void showFilter(String selectOption, String searchText){
        synchronized (model) {
            // 清空model后，根据URL来做匹配
            model.setRowCount(0);

            // 判断当前历史记录是否为空
            if((selectOption.equals("全部"))){
                historySearchText = searchText;
            }

            // 遍历apiDataModelMap
            for (Map.Entry<String, ApiDataModel> entry : IProxyScanner.apiDataModelMap.entrySet()) {
                String url = entry.getKey();
                ApiDataModel apiDataModel = entry.getValue();
                if (selectOption.equals("只看status为200") && !apiDataModel.getStatus().contains("200")){
                    continue;
                } else if (selectOption.equals("只看重点") &&  !apiDataModel.getHavingImportant()) {
                    continue;
                } else if (selectOption.equals("只看敏感内容") && !apiDataModel.getResult().contains("敏感内容")){
                    continue;
                } else if (selectOption.equals("只看敏感路径") && !apiDataModel.getResult().contains("敏感路径")) {
                    continue;
                }
                if (url.toLowerCase().contains(searchText.toLowerCase())) {
                    model.insertRow(0, new Object[]{
                            Constants.TREE_STATUS_COLLAPSE,
                            apiDataModel.getId(),
                            apiDataModel.getUrl(),
                            apiDataModel.getPATHNumber(),
                            apiDataModel.getMethod(),
                            apiDataModel.getStatus(),
                            apiDataModel.getIsJsFindUrl(),
                            apiDataModel.getHavingImportant(),
                            apiDataModel.getResult(),
                            apiDataModel.getDescribe(),
                            apiDataModel.getTime()
                    });
                }
            }
        }
    }

    public static void clearAllData(){
        synchronized (model) {
            // 清空model
            model.setRowCount(0);
            // 清空表格
            IProxyScanner.apiDataModelMap = new HashMap<String, ApiDataModel>();
            IProxyScanner.setHaveScanUrlNew();
            // 清空检索
            historySearchText = "";
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ConfigPanel.searchField.setText("");
                }
            });

            // 还可以清空编辑器中的数据
            MailPanel.requestTextEditor.setMessage(new byte[0], true); // 清空请求编辑器
            MailPanel.responseTextEditor.setMessage(new byte[0], false); // 清空响应编辑器
            MailPanel.resultDeViewer.setText(new byte[0]);
            MailPanel.currentlyDisplayedItem = null; // 清空当前显示的项
        }
    }

    public void modelExpand(ApiDataModel apiDataModel, int index) {
        // 关闭自动更新
        ConfigPanel.setFlashButtonFalse();
        // 看当前是否有过滤场景
        String selectedOption = (String)ConfigPanel.choicesComboBox.getSelectedItem();


        model.setValueAt(Constants.TREE_STATUS_EXPAND, index, 0);

        Map<String, Object> pathData = apiDataModel.getPathData();

        int tmpIndex = 0;
        for (Map.Entry<String, Object> pathEntry : pathData.entrySet()) {
            Map<String, Object> subPathValue = (Map<String, Object>)pathEntry.getValue();
            if (selectedOption.equals("只看status为200") && !((String)subPathValue.get("status")).contains("200")){
                continue;
            } else if (selectedOption.equals("只看重点") &&  !((Boolean) subPathValue.get("isImportant"))) {
                continue;
            } else if (selectedOption.equals("只看敏感内容") && !((String)subPathValue.get("result")).contains("敏感内容")){
                continue;
            } else if (selectedOption.equals("只看敏感路径") && !((String)subPathValue.get("result")).contains("敏感路径")) {
                continue;
            }
            tmpIndex += 1;
            String listStatus;

            if (tmpIndex != pathData.size() && pathData.size() != 1) {
                listStatus = "┠";
            } else if (pathData.size() == 1) {
                listStatus = "┗";
            } else {
                listStatus = "┗";
            }
            model.insertRow(index+tmpIndex, new Object[]{
                    listStatus,
                    "-",
                    pathEntry.getKey(),
                    "-",
                    subPathValue.get("method"),
                    subPathValue.get("status"),
                    subPathValue.get("isJsFindUrl"),
                    subPathValue.get("isImportant"),
                    subPathValue.get("result"),
                    subPathValue.get("describe"),
                    subPathValue.get("time")
            });
            model.fireTableRowsInserted(index+tmpIndex, index+tmpIndex);
        }
        // 通知监听器，从selfIndex + 1 到 selfIndex + subApiData.size()的行已经被插入
        model.fireTableRowsInserted(index + 1, index + pathData.size());

    }

    public void modeCollapse(ApiDataModel apiDataModel, int index) {
        // 看当前是否有过滤场景
        String selectedOption = (String)ConfigPanel.choicesComboBox.getSelectedItem();
        model.setValueAt(Constants.TREE_STATUS_COLLAPSE, index, 0);

        Map<String, Object> pathData = apiDataModel.getPathData();
        // 计算即将删除的行区间
        int startDeleteIndex = index + 1;
        int deleteNumber = 0;

        // 从后向前删除子项，这样索引就不会因为列表的变动而改变
        int numberOfRows = model.getRowCount();
        for (int i = 0; i < numberOfRows; i++) {
            try {
                if (!model.getValueAt(startDeleteIndex, 0).equals(Constants.TREE_STATUS_EXPAND) && !model.getValueAt(startDeleteIndex, 0).equals(Constants.TREE_STATUS_COLLAPSE)) {
                    model.removeRow(startDeleteIndex);
                    deleteNumber += 1;
                } else {
                    break;
                }} catch (Exception e) {
                    // 捕获其他所有类型的异常
                    BurpExtender.getStdout().println("Exception caught: " + e.getMessage());
                }
        }

        // 现在所有的子项都被删除了，通知表格模型更新
        // 注意这里的索引是根据删除前的状态传递的
        model.fireTableRowsDeleted(startDeleteIndex, index+deleteNumber);
    }

    public int findRowIndexByURL(String url) {
        for (int i = 0; i < model.getRowCount(); i++) {
            // 获取每一行第二列的值
            Object value = model.getValueAt(i, 2);
            // 检查这个值是否与要查找的URL匹配
            if (value != null && value.equals(url)) {
                // 如果匹配，返回当前行的索引
                return i;
            }
        }
        // 如果没有找到，返回-1表示未找到
        return -1;
    }

    public String findUrlFromPath(int row){
        for (int index = row; index >= 0; index--) {
            // 获取每一行第二列的值
            String value = (String)model.getValueAt(index, 0);
            if (value.equals(Constants.TREE_STATUS_EXPAND) || value.equals((Constants.TREE_STATUS_COLLAPSE))){
                return (String)model.getValueAt(index, 2);
            }
        }
        return null;
    }

    public DefaultTableModel getModel(){
        return model;
    }

    public ConfigPanel getConfigPanel(){
        return this.configPanel;
    }

}
