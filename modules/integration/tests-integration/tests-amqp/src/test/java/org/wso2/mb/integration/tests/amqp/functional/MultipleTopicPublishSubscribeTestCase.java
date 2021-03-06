/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.mb.integration.tests.amqp.functional;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.mb.integration.common.clients.AndesClient;
import org.wso2.mb.integration.common.clients.operations.utils.AndesClientUtils;

/**
 * 1. use two topics t1, t2. 2 subscribers for t1 and one subscriber for t2
 * 2. use two publishers for t1 and one for t2
 * 3. check if messages were received correctly
 */
public class MultipleTopicPublishSubscribeTestCase {

    @BeforeClass
    public void prepare() {
        AndesClientUtils.sleepForInterval(15000);
    }

    @Test(groups = {"wso2.mb", "topic"})
    public void performMultipleTopicPublishSubscribeTestCase() {

        Integer sendCount1 = 1000;
        Integer sendCount2 = 2000;
        Integer runTime = 40;
        int additional = 10;

        //expect little more to check if no more messages are received
        Integer expectedCount2 = 4000 + additional;
        Integer expectedCount1 = 1000 + additional;

        AndesClient receivingClient2 = new AndesClient("receive", "127.0.0.1:5672", "topic:multipleTopic2,", "100",
                "false",
                runTime.toString(), expectedCount2.toString(), "2",
                "listener=true,ackMode=1,delayBetweenMsg=0,stopAfter=" + expectedCount2, "");

        AndesClient receivingClient1 = new AndesClient("receive", "127.0.0.1:5672", "topic:multipleTopic1,", "100",
                "false",
                runTime.toString(), expectedCount1.toString(), "1",
                "listener=true,ackMode=1,delayBetweenMsg=0,stopAfter=" + expectedCount1, "");

        receivingClient1.startWorking();
        receivingClient2.startWorking();


        AndesClient sendingClient2 = new AndesClient("send", "127.0.0.1:5672", "topic:multipleTopic2", "100",
                "false", runTime.toString(), sendCount2.toString(), "2",
                "ackMode=1,delayBetweenMsg=0,stopAfter=" + sendCount2, "");

        AndesClient sendingClient1 = new AndesClient("send", "127.0.0.1:5672", "topic:multipleTopic1", "100",
                "false", runTime.toString(), sendCount1.toString(), "1",
                "ackMode=1,delayBetweenMsg=0,stopAfter=" + sendCount1, "");

        sendingClient1.startWorking();
        sendingClient2.startWorking();

        AndesClientUtils.waitUntilMessagesAreReceived(receivingClient1, expectedCount1, runTime);
        AndesClientUtils.waitUntilMessagesAreReceived(receivingClient2, expectedCount2, runTime);

        Assert.assertEquals(receivingClient1.getReceivedTopicMessagecount(), expectedCount1 - additional,
                "Did not receive expected message count for multipleTopic1.");
        Assert.assertEquals(receivingClient2.getReceivedTopicMessagecount(), expectedCount2 - additional,
                "Did not receive expected message count for multipleTopic2.");
    }
}
