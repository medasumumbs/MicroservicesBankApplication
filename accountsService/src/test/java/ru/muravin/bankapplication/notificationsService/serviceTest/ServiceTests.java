package ru.muravin.bankapplication.notificationsService.serviceTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*@SpringBootTest(classes = {TestApplicationConfiguration.class})
@Import(TestcontainersConfiguration.class)
@ContextConfiguration(classes = TestApplicationConfiguration.class)
@TestPropertySource(locations = "classpath:application.yml")
@AutoConfigureWebTestClient
@ExtendWith(MockitoExtension.class)
@Disabled*/
public class ServiceTests {
    /*@MockitoBean
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationsRepository notificationsRepository;

    @Test
    @Disabled
    void testSave() {
        notificationsRepository.deleteAll();
        var localDateTime = LocalDateTime.now();
        NotificationDto notificationDto = new NotificationDto("abcde","a",localDateTime);
        userService.sendNotification(notificationDto);
        var notification = notificationsRepository.findAll().get(0);
        assertEquals("abcde", notification.getMessage());
        assertEquals("a", notification.getSender());
        assertEquals(localDateTime,notification.getCreatedAt());
    }*/
}
