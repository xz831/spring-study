package com.xz.spring;

/**
 * @Package: com.xz.spring
 * @ClassName: UserController
 * @Author: xz
 * @Date: 2020/6/21 14:06
 * @Version: 1.0
 */
@Component
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private OrderService orderService;
}
