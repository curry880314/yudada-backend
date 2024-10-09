package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import generator.domain.Question;
import generator.service.QuestionService;
import com.yupi.yudada.mapper.QuestionMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【question(题目)】的数据库操作Service实现
* @createDate 2024-10-09 23:51:00
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService{

}




