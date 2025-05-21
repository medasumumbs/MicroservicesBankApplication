package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should create a notification and return 201 with response DTO"

    request {
        method 'POST'
        url '/notifications'
        body([
                message: "Test message",
                sender: "System",
                timestamp: "2025-04-05T10:00:00"
        ])
        headers {
            contentType('application/json')
        }
    }

    response {
        status 201
        body([
                statusCode: "OK",
                statusMessage: "Notification created",
                notificationId: value(producer(regex('[0-9]+'))) // ID — любое число
        ])
        headers {
            contentType('application/json')
        }
    }
}