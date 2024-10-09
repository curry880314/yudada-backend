package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import generator.domain.UserAnswer;
import generator.service.UserAnswerService;
import com.yupi.yudada.mapper.UserAnswerMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【user_answer(用户答题记录)】的数据库操作Service实现
* @createDate 2024-10-09 23:51:00
*/
@Service
public class UserAnswerServiceImpl extends ServiceImpl<UserAnswerMapper, UserAnswer>
    implements UserAnswerService{

}




