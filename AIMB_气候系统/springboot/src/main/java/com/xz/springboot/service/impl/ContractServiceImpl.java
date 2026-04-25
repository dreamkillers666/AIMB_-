package com.xz.springboot.service.impl;

import com.xz.springboot.entity.Contract;
import com.xz.springboot.mapper.ContractMapper;
import com.xz.springboot.service.IContractService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xz
 * @since 2023-04-22
 */
@Service
public class ContractServiceImpl extends ServiceImpl<ContractMapper, Contract> implements IContractService {

}
