package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import generator.domain.ScoringResult;
import generator.service.ScoringResultService;
import com.yupi.yudada.mapper.ScoringResultMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【scoring_result(评分结果)】的数据库操作Service实现
* @createDate 2024-10-09 23:51:00
*/
@Service
public class ScoringResultServiceImpl extends ServiceImpl<ScoringResultMapper, ScoringResult>
    implements ScoringResultService{

}




