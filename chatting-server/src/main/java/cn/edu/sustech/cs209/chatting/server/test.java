package cn.edu.sustech.cs209.chatting.server;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;

public class test extends Application {

  @Override
  public void start(Stage stage) {
    // 创建多选框
    CheckBox option1 = new CheckBox("选项1");
    CheckBox option2 = new CheckBox("选项2");
    CheckBox option3 = new CheckBox("选项3");

    // 创建确认按钮
    Button confirmButton = new Button("确认");
    confirmButton.setOnAction(event -> {
      // 获取所有选中的选项
      ArrayList<String> selectedOptions = new ArrayList<>();
      if (option1.isSelected()) {
        selectedOptions.add("选项1");
      }
      if (option2.isSelected()) {
        selectedOptions.add("选项2");
      }
      if (option3.isSelected()) {
        selectedOptions.add("选项3");
      }

      // 打印选中的选项
      System.out.println("您选择了：" + selectedOptions);
    });

    // 创建布局
    VBox layout = new VBox(10, option1, option2, option3, confirmButton);
    layout.setPadding(new Insets(10));

    // 创建场景
    Scene scene = new Scene(layout, 250, 150);

    // 设置舞台
    stage.setTitle("多选窗口");
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}
