package com.rtalky.application.event;

import com.rtalky.domain.event.UserRegisteredEvent;
import com.rtalky.domain.model.Note;
import com.rtalky.domain.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 用户注册事件处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredEventHandler {

    private final NoteRepository noteRepository;

    /**
     * 处理用户注册成功事件 - 发送欢迎消息
     */
    @Async
    @EventListener
    public void handleWelcomeMessage(UserRegisteredEvent event) {
        try {
            log.info("发送欢迎消息给新用户: userId={}, phoneNumber={}",
                    event.getUserId(), event.getPhoneNumber());

            // 模拟发送欢迎短信
            // smsClient.sendWelcomeMessage(event.getPhoneNumber(), event.getUserId());

            log.info("欢迎消息发送成功: userId={}", event.getUserId());
        } catch (Exception e) {
            log.error("发送欢迎消息失败: userId={}", event.getUserId(), e);
        }
    }

    /**
     * 处理用户注册成功事件 - 初始化用户资料
     */
    @Async
    @EventListener
    public void handleProfileInitialization(UserRegisteredEvent event) {
        try {
            log.info("初始化用户资料: userId={}", event.getUserId());

            // 初始化默认昵称（手机号脱敏）
            String maskedPhone = event.getPhoneNumber().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
            // userRepository.updateNickname(event.getUserId(), "用户_" + maskedPhone);

            log.info("用户资料初始化完成: userId={}", event.getUserId());
        } catch (Exception e) {
            log.error("初始化用户资料失败: userId={}", event.getUserId(), e);
        }
    }

    /**
     * 处理用户注册成功事件 - 注册统计
     */
    @EventListener
    public void handleRegistrationStatistics(UserRegisteredEvent event) {
        try {
            log.info("记录用户注册统计: userId={}, authMethod={}",
                    event.getUserId(), event.getAuthMethod());

            // 记录注册渠道统计
            // statisticsService.recordRegistration(event.getAuthMethod());

        } catch (Exception e) {
            log.error("记录注册统计失败: userId={}", event.getUserId(), e);
        }
    }

    /**
     * 处理用户注册成功事件 - 创建初始帮助笔记
     */
    @Async
    @EventListener
    public void handleInitialNotesCreation(UserRegisteredEvent event) {
        try {
            log.info("为新用户创建初始帮助笔记: userId={}", event.getUserId());
            
            String userId = event.getUserId();
            
            // 创建3篇帮助笔记
            createHelpNote(userId, "笔记管理指南", 
                "详细介绍 RTalky 笔记管理功能的使用方法，包括浏览、创建、编辑和阅读笔记的完整流程。",
                getNoteManagementContent(),
                Arrays.asList("笔记管理", "使用指南", "基础功能"));
                
            createHelpNote(userId, "个人信息管理",
                "指导用户如何在个人中心管理个人信息，包括头像设置、昵称修改和简介编辑等功能。",
                getPersonalInfoContent(),
                Arrays.asList("个人中心", "头像设置", "个人信息"));
                
            createHelpNote(userId, "主题设置指南",
                "详细介绍如何设置和切换应用主题，包括浅色、深色和跟随系统三种主题模式的使用方法。",
                getThemeSettingsContent(),
                Arrays.asList("主题设置", "个性化", "界面美化"));
            
            log.info("初始帮助笔记创建完成: userId={}", event.getUserId());
        } catch (Exception e) {
            log.error("创建初始帮助笔记失败: userId={}", event.getUserId(), e);
        }
    }
    
    /**
     * 创建帮助笔记
     */
    private void createHelpNote(String userId, String title, String description, String content, List<String> tags) {
        try {
            Note note = Note.newNote(userId, title, description, content, tags);
            noteRepository.save(note);
            log.debug("帮助笔记创建成功: userId={}, title={}", userId, title);
        } catch (Exception e) {
            log.error("创建帮助笔记失败: userId={}, title={}", userId, title, e);
        }
    }
    
    /**
     * 笔记管理指南内容
     */
    private String getNoteManagementContent() {
        return "## 欢迎使用 RTalky 笔记管理\n\n" +
               "### 浏览和管理笔记\n\n" +
               "在笔记库界面，你可以通过上下滑动浏览所有笔记卡片的简略信息。每个笔记卡片都显示了笔记的基本信息，让你快速了解笔记内容。\n\n" +
               "### 笔记设置操作\n\n" +
               "点击任意笔记卡片右上角的设置按钮，即可打开该笔记的专属设置界面。在这里你可以：\n" +
               "- 修改笔记名称，让笔记更容易识别\n" +
               "- 编辑笔记介绍，为笔记添加简要说明\n" +
               "- 删除不需要的笔记，保持笔记库整洁\n\n" +
               "### 创建新笔记\n\n" +
               "点击笔记库导航栏右侧的\"新增\"按钮，进入笔记创建流程。系统会引导你分步骤完成：\n" +
               "1. 编写笔记名称\n" +
               "2. 添加笔记介绍\n" +
               "3. 使用 Markdown 语言编写笔记内容\n" +
               "4. 创建和管理笔记标签\n\n" +
               "### 阅读笔记\n\n" +
               "点击笔记卡片进入笔记阅读器，享受流畅的阅读体验：\n" +
               "- 竖直滑动浏览笔记内容\n" +
               "- 水平滑动或使用按键翻页\n" +
               "- 倾斜手机实现翻页（在笔记阅读界面下方中间，有触发按键，点击下方滑出解锁器，长按解锁器可以解锁倾斜翻页服务）\n\n" +
               "### 编辑笔记\n\n" +
               "在笔记阅读器界面的导航栏右侧，有编辑笔记功能，可以编辑修改当前笔记内容。\n\n" +
               "### Markdown 支持\n\n" +
               "目前支持以下 Markdown 语法：\n" +
               "- 标题（Headings）\n" +
               "- 强调文本（Emphasis）\n" +
               "- 引用块（Blockquotes）\n" +
               "- 列表（Lists）\n" +
               "- 代码块（Code）\n" +
               "- 表格（Tables）\n" +
               "- 链接（Links）";
    }
    
    /**
     * 个人信息管理内容
     */
    private String getPersonalInfoContent() {
        return "## 个人中心使用指南\n\n" +
               "### 进入个人中心\n\n" +
               "有两种方式可以打开个人中心：\n" +
               "- 点击应用主页导航栏的头像\n" +
               "- 在主页右滑屏幕滑出个人中心面板\n\n" +
               "### 编辑个人信息\n\n" +
               "在个人中心的个人信息编辑区域，你可以完善个人资料：\n\n" +
               "### 头像设置\n" +
               "- 点击头像编辑按钮（在头像右下方），会弹出头像操作功能区\n" +
               "- 从相册获取：选择\"从相册选择\"，浏览并选择你喜欢的照片\n" +
               "- 拍照获取：选择\"拍照\"功能，使用相机拍摄新头像\n" +
               "- 保存头像：可以将当前头像保存到本地相册\n\n" +
               "### 个人资料\n" +
               "- 修改个人昵称：点击昵称区域，输入你喜欢的昵称\n" +
               "- 编辑个人简介：添加个人介绍，让其他用户更好地了解你\n\n" +
               "### 保存更改\n\n" +
               "完成编辑后，记得点击保存按钮确认更改。你的个人信息将实时更新，并在整个应用中生效。";
    }
    
    /**
     * 主题设置指南内容
     */
    private String getThemeSettingsContent() {
        return "## 个性化主题设置\n\n" +
               "### 进入主题设置\n\n" +
               "在个人中心找到\"主题设置\"栏目，点击进入主题修改界面。\n\n" +
               "### 主题选择\n\n" +
               "系统提供多种主题选项：\n" +
               "- **浅色主题**：明亮清爽的界面风格，适合日间使用\n" +
               "- **深色主题**：护眼的深色界面，适合夜间阅读\n" +
               "- **跟随系统**：自动根据系统设置切换主题\n\n" +
               "### 预览体验\n\n" +
               "在选择主题前，你可以先预览效果：\n" +
               "- 点击任意主题选项查看预览\n" +
               "- 在预览界面体验不同主题的视觉效果\n" +
               "- 确认满意后再进行应用\n\n" +
               "### 应用主题\n\n" +
               "预览满意后，点击\"确定\"按钮应用所选主题，系统会自动切换界面风格并返回个人中心。主题设置会立即生效，为你的使用体验带来全新的视觉感受。";
    }
}
