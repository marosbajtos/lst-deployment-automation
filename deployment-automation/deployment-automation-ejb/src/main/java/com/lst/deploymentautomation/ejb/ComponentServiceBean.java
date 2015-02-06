/*
 *********************************************************************

 $Id$


 Copyright (c) 2007-2014 Whitestein Technologies AG,
 Riedstrasse 13, CH-6330 Cham, Switzerland.
 All rights reserved.

 This software is confidential and proprietary information of
 Whitestein Technologies AG.
 You shall not disclose this confidential information and shall use
 it only in accordance with the terms of the license agreement you
 entered into with Whitestein Technologies AG.
 The use of this file in source or binary form requires a written
 license from Whitestein Technologies AG.
 *********************************************************************
 */
package com.lst.deploymentautomation.ejb;

import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import com.whitestein.lsps.common.ComponentServiceBase;
import com.whitestein.lsps.common.ComponentServiceLocal;
import com.whitestein.lsps.engine.AsynchronousExecutionTask;

/**
 * The central class for registering custom EJB components (custom LSPS tasks and functions).
 * The components have to be registered inside {@link ComponentServiceBean#registerCustomComponents()} method.
 * See a commented snippet in the source code for an example. 
 * POJO implementations of tasks and functions do not have to be registered.
 */
@Stateless
@PermitAll
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class ComponentServiceBean extends ComponentServiceBase implements ComponentServiceLocal {

	@EJB(beanName = "SystemCallTask")
	private AsynchronousExecutionTask systemCallTask;

	@Override
	protected void registerCustomComponents() {
		register(systemCallTask, SystemCallTask.class);
	}

	/*
	 * To register a custom EJB task (e.g. com.whitestein.lsps.custom.MyCustomTask)
	 * the target EJB has to be injected using its local interface ExecutableTask
	 * and specifying beanName attribute (which is by default non-qualified class name of its implementation). 
	 * The instance of the given EJB task must be then registered under its implementation class.
	 * The task is then referenced in the model by its fully qualified implementation task name.
	 * 
	 * To register custom EJB functions (e.g. com.whitestein.lsps.custom.MyCustomFunctions)
	 * the target EJB has to be injected using its local interface (the local interface must declare
	 * all the functions that will be used in the model).
	 * The instance of the given EJB must be then registered under its local interface class.
	 * The target function is then referenced in the model using its fully qualified local interface name and method name.
	 * 


	@EJB(beanName = "MyCustomTask")
	private ExecutableTask myCustomTask;

	@EJB
	private MyFunctions myFunctions;


	@Override
	protected void registerCustomComponents() {
		register(myCustomTask, MyCustomTask.class);
		register(myFunctions, MyFunctions.class);
	}
	*/

}
