# ccTalk-Java
implementation of the ccTalk protocol in Java www.cctalk.org
#### dependencies
this work uses JSerialComm: http://fazecast.github.io/jSerialComm/

## Tutorial
Here some simple examples.
* To create a message(simple poll):

```
	CCTalkMessage message = new CCTalkMessage(2,1,254,null):
```

* Create a message with data:

```
	CCTalkMessage message = new CCTalkMessage(2,1,209,new byte[]{
    							(byte)1
                                                                }):
```





 * Establish connection:

```
	CCTalkConnection connection = new CCTalkConnection("/dev/USB0");
```

* send Message and wait for response:

```
	List<CCTalkMessage> response = connection.sendMessage(2,1,254,null);
```

* close connection

```
	connection.close();
```
