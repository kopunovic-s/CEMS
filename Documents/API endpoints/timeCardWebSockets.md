# Time Card WebSockets

## Connection Link

```
ws://coms-3090-024.class.las.iastate.edu:8080/clockEvents/{companyId}/{userId}
```

## Implementation Notes

- Logging in or clicking on the clock in button connects to the websocket or something
- when the user clicks the clock in button it sends a message (name: CLOCK_IN) to the server
- When user clicks the clock out button it sends a message (name: CLOCK_OUT) to the server
- So basically the clock in and out button will send 2 requests, the first request is to send the info to the API (user/timecard/clockIn/Out). The second request is to send the message to the websocket server (name: CLOCK_IN or CLOCK_OUT).

## Message Types

### Clock In

```
{Name}: CLOCK_IN
Cai Chen: CLOCK_IN
```

### Clock Out

```
{Name}: CLOCK_OUT
Cai Chen: CLOCK_OUT
```

Example Returns:

```
Cai Chen clocked out at 01:48, 04-06-2025
Cai Chen clocked in at 01:34, 04-06-2025
```
