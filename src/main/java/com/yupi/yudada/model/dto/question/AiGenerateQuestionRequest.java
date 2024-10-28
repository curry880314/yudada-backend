package com.yupi.yudada.model.dto.question;

import java.io.Serializable;

/**
 *
 * AI生成题目请求类
 *
 * @author liyongkang
 * @since
 */
public class AiGenerateQuestionRequest implements Serializable {


    /**
     * 应用id
     */
    private Long appId;

    /**
     * 题目数量
     */
    int quetionNumber=10;

    /**
     * 选项数量
     */
    int optionNumber=2;


    private static final long serialVersionUID = 1L;
}
