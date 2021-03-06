package com.orderlist.controller;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;


import com.bell.model.BellVO;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.member.model.MemberService;
import com.member.model.MemberVO;
import com.orderdetail.model.OrderdetailService;
import com.orderdetail.model.OrderdetailVO;
import com.orderlist.model.*;
import com.product.model.ProductService;
import com.qrcode.OrderListQRCodeCreate;
import com.websocket.WebSocket;

import redis.clients.jedis.Jedis;

@WebServlet("/orderlist")
public class orderlistServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		String action = req.getParameter("action");
		
		if("getMemberKun".equals(action)) {
			try {
				//拿到登入ID
				HttpSession session =req.getSession();
				String m_id = session.getAttribute("loginId").toString();

			    //拿到memberVO
			    MemberService memSvc = new MemberService();
				MemberVO memberVO = memSvc.findOneMem(m_id);
			    session.setAttribute("memberVO", memberVO);
			    
			    //用訂單service拿到屬於會員的訂單
				OrderlistService orderlistSvc =new OrderlistService();
				List<OrderlistVO> list = orderlistSvc.findByMember(m_id);
				req.setAttribute("list", list);				

				String url = "/Front_end/members/MyKunCoin.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url); // 轉交
				successView.forward(req, res);

				/*************************** 其他可能的錯誤處理 *************************************/
			} catch (Exception e) {
//				errorMsgs.add("無法取得資料:" + e.getMessage());
				RequestDispatcher failureView = req.getRequestDispatcher("/Back_end/orderlist/select_page.jsp");
				failureView.forward(req, res);
			}
				
		}
				
			

		//拿會員全部訂單
		if("getMemberAll".equals(action)) {
		    
			try {
			//拿到登入ID
			HttpSession session =req.getSession();
			String m_id = session.getAttribute("loginId").toString();

		    //拿到memberVO
		    MemberService memSvc = new MemberService();
			MemberVO memberVO = memSvc.findOneMem(m_id);
		    session.setAttribute("memberVO", memberVO);
		    
		    //用訂單service拿到屬於會員的訂單
			OrderlistService orderlistSvc =new OrderlistService();
			List<OrderlistVO> list = orderlistSvc.findByMember(m_id);
			
			//拿到"訂單成立"集合
			List<OrderlistVO> neworder = new ArrayList<OrderlistVO>();
			for(int i=0;i<list.size();i++) {
				if(list.get(i).getO_status().equals("訂單成立")) {
					neworder.add(list.get(i));
				}
			}
			
			//拿到"已出貨"集合
			List<OrderlistVO> sentorder = new ArrayList<OrderlistVO>();
			for(int i=0;i<list.size();i++) {
				if(list.get(i).getO_status().equals("已出貨")) {
					sentorder.add(list.get(i));
				}
			}
			
			//拿到"已到貨"集合
			List<OrderlistVO> arrivedorder = new ArrayList<OrderlistVO>();
			for(int i=0;i<list.size();i++) {
				if(list.get(i).getO_status().equals("已到貨")) {
					arrivedorder.add(list.get(i));
				}
			}
			
			//拿到"訂單完成"集合
			List<OrderlistVO> finishorder = new ArrayList<OrderlistVO>();
			for(int i=0;i<list.size();i++) {
				if(list.get(i).getO_status().equals("訂單完成")) {
					finishorder.add(list.get(i));
				}
			}
			
			
			req.setAttribute("list", list);
			
			req.setAttribute("neworder", neworder);
			req.setAttribute("sentorder", sentorder);
			req.setAttribute("arrivedorder", arrivedorder);
			req.setAttribute("finishorder", finishorder);
			

			String url = "/Front_end/members/MyOrder.jsp";
			RequestDispatcher successView = req.getRequestDispatcher(url); // 轉交
			successView.forward(req, res);

			/*************************** 其他可能的錯誤處理 *************************************/
		} catch (Exception e) {
//			errorMsgs.add("無法取得資料:" + e.getMessage());
			RequestDispatcher failureView = req.getRequestDispatcher("/Front_end/members/MyOrder.jsp");
			failureView.forward(req, res);
		}
			
	}
		
		
			
//拿賣家訂單
		if("getSellerAll".equals(action)) {
		    
			try {
			//拿到登入ID
			HttpSession session =req.getSession();
			String m_id = session.getAttribute("loginId").toString();

		    
		      //找所有訂單
			   OrderlistService olsv =new OrderlistService();
			   List<OrderlistVO> list = olsv.getAll();
			   OrderdetailService odsv =new OrderdetailService();
			   ProductService psvc =new ProductService();
			   
			   //賣家所有訂單集合
			   List<OrderlistVO> sellerorder = new ArrayList<OrderlistVO>();
			    //"訂單成立"集合
				List<OrderlistVO> neworder = new ArrayList<OrderlistVO>();
				//"已出貨"集合
				List<OrderlistVO> sentorder = new ArrayList<OrderlistVO>();
				//"已到貨"集合
				List<OrderlistVO> arrivedorder = new ArrayList<OrderlistVO>();
				//"訂單完成"集合
				List<OrderlistVO> finishorder = new ArrayList<OrderlistVO>();
				
//				//"QRcode"集合
//				List qrcodelist = new ArrayList();
			   
			   if (list.size() != 0) {
					for (int i = 0; i < list.size(); i++) {
//						System.out.println("訂單號"+(list.get(i).getO_id())+"，賣家:"+psvc.oneProduct(odsv.getFirstP_id(list.get(i).getO_id())).getM_id());
						//從全訂單比對有該帳戶的訂單
						String comparem_id = psvc.oneProduct(odsv.getFirstP_id(list.get(i).getO_id())).getM_id();
						String compareo_status = list.get(i).getO_status();
						
						
//						//生成QRcode
//						String hostString = req.getServerName() + ":" + req.getServerPort();
//						System.out.println(hostString);
//						
//						OrderListQRCodeCreate qr = new OrderListQRCodeCreate();
//						qrcodelist.add((qr.creater(hostString, list.get(i).getO_id())));

						if (comparem_id.equals(m_id)) {
							sellerorder.add(list.get(i));
						}
						if (comparem_id.equals(m_id) && compareo_status.trim().equals("訂單成立")) {
							neworder.add(list.get(i));
						}
						if (comparem_id.equals(m_id) && compareo_status.trim().equals("已出貨")) {
							sentorder.add(list.get(i));							
						}
						if (comparem_id.equals(m_id) && compareo_status.trim().equals("已到貨")) {
							arrivedorder.add(list.get(i));
						}
						if (comparem_id.equals(m_id) && compareo_status.trim().equals("訂單完成")) {
							finishorder.add(list.get(i));
						}
					}
				}
			   
			req.setAttribute("sellerorder", sellerorder);
			req.setAttribute("neworder", neworder);
			req.setAttribute("sentorder", sentorder);
			req.setAttribute("arrivedorder", arrivedorder);
			req.setAttribute("finishorder", finishorder);
			
//			System.out.println("qrcode集合在c"+qrcodelist);
//			req.setAttribute("qrcodelist", qrcodelist);
		    

			

			String url = "/Front_end/order/SellerOrder.jsp";
			RequestDispatcher successView = req.getRequestDispatcher(url); // 轉交
			successView.forward(req, res);

			/*************************** 其他可能的錯誤處理 *************************************/
		} catch (Exception e) {
//			errorMsgs.add("無法取得資料:" + e.getMessage());
			RequestDispatcher failureView = req.getRequestDispatcher("/Front_end/order/SellerOrder.jsp");
			failureView.forward(req, res);
		}
			
	}		
		
//修改訂單狀態為已出貨
		if("change_O_status".equals(action)) {
			try {
				String o_status= req.getParameter("o_status");
				String o_id =req.getParameter("o_id");
				
				OrderlistVO orderlistVO = new OrderlistVO();
				orderlistVO.setO_status(o_status);
				orderlistVO.setO_id(o_id);
				
				OrderlistService orderlistSvc = new OrderlistService();
				orderlistVO = orderlistSvc.updateStatus(o_status, o_id);
				OrderdetailService orderdetailSvc = new OrderdetailService();
				List<OrderdetailVO> list = orderdetailSvc.getDetailByOrder(o_id);
				
				//推播開始--------------------------
				Jedis jedis = new Jedis("localhost", 6379);
				jedis.auth("123456");
				
				ObjectMapper mapper = new ObjectMapper();
				WebSocket ws = new WebSocket();
				BellVO bellVO = new BellVO();
				String m_id = orderlistSvc.getOneOrderlist(o_id).getM_id();
				
				bellVO.setM_id(m_id);
				bellVO.setMessage("您的訂單"+o_id+o_status);
				
				ws.onMessage(mapper.writeValueAsString(bellVO));
				
				jedis.close();
				//推播結束--------------------------
					
				req.setAttribute("orderlistVO", orderlistVO);
				req.setAttribute("list", list);
			    String url = "/Back_end/OrderDetail/listOrderdetailByOrder.jsp";
			    RequestDispatcher successView = req.getRequestDispatcher(url); 
			    successView.forward(req, res);

		} catch (Exception e) {
			RequestDispatcher failureView = req.getRequestDispatcher("/Back_end/OrderDetail/listOrderdetailByOrder.jsp");
			failureView.forward(req, res);
		}
					
}
		

		
//查會員訂單action		
		if ("getMember_For_Display".equals(action)) { // 來自select_page.jsp的請求

			List<String> errorMsgs = new LinkedList<String>();
			req.setAttribute("errorMsgs", errorMsgs);

			try {
				/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 **********************/
				String str = req.getParameter("m_id");

				if (!errorMsgs.isEmpty()) {
					RequestDispatcher failureView = req.getRequestDispatcher("/Back_end/orderlist/select_page.jsp");
					failureView.forward(req, res);
					return;// 程式中斷
				}

				String m_id = null;
				try {
					m_id = str;
				} catch (Exception e) {
					errorMsgs.add("編號格式不正確");
				}
				// Send the use back to the form, if there were errors
				if (!errorMsgs.isEmpty()) {
					RequestDispatcher failureView = req.getRequestDispatcher("/Back_end/orderlist/select_page.jsp");
					failureView.forward(req, res);
					return;// 程式中斷
				}

				/*************************** 2.開始查詢資料 *****************************************/
				OrderlistService orderlistSvc =new OrderlistService();
				List<OrderlistVO> list = orderlistSvc.findByMember(m_id);
				req.setAttribute("list", list);
				
				if (!errorMsgs.isEmpty()) {
					RequestDispatcher failureView = req
							.getRequestDispatcher("/Back_end/orderlist/listOneorderlist.jsp");
					failureView.forward(req, res);
					return;// 程式中斷
				}

				/*************************** 3.查詢完成,準備轉交(Send the Success view) *************/
				String url = "/Back_end/orderlist/listOrderlistByMember.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url); // 成功轉交 listOneEmp.jsp
				successView.forward(req, res);

				/*************************** 其他可能的錯誤處理 *************************************/
			} catch (Exception e) {
				errorMsgs.add("無法取得資料:" + e.getMessage());
				RequestDispatcher failureView = req.getRequestDispatcher("/Back_end/orderlist/select_page.jsp");
				failureView.forward(req, res);
			}
		}
		
		
		
		

		if ("getOne_For_Display".equals(action)) { // 來自select_page.jsp的請求

			List<String> errorMsgs = new LinkedList<String>();
			// Store this set in the request scope, in case we need to
			// send the ErrorPage view.
			req.setAttribute("errorMsgs", errorMsgs);

			try {
				/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 **********************/
				String str = req.getParameter("o_id");
				if (str == null || (str.trim()).length() == 0) {
					errorMsgs.add("請輸入編號");
				}
				// Send the use back to the form, if there were errors
				if (!errorMsgs.isEmpty()) {
					RequestDispatcher failureView = req.getRequestDispatcher("/Back_end/orderlist/select_page.jsp");
					failureView.forward(req, res);
					return;// 程式中斷
				}

				String o_id = null;
				try {
					o_id = str;
				} catch (Exception e) {
					errorMsgs.add("編號格式不正確");
				}
				// Send the use back to the form, if there were errors
				if (!errorMsgs.isEmpty()) {
					RequestDispatcher failureView = req.getRequestDispatcher("/Back_end/orderlist/select_page.jsp");
					failureView.forward(req, res);
					return;// 程式中斷
				}

				/*************************** 2.開始查詢資料 *****************************************/
				OrderlistService orderlistSvc = new OrderlistService();
				OrderlistVO orderlistVO = orderlistSvc.getOneOrderlist(o_id);
				if (orderlistVO == null) {
					errorMsgs.add("查無資料");
				}
				// Send the use back to the form, if there were errors
				if (!errorMsgs.isEmpty()) {
					RequestDispatcher failureView = req
							.getRequestDispatcher("/Back_end/orderlist/listOneorderlist.jsp");
					failureView.forward(req, res);
					return;// 程式中斷
				}

				/*************************** 3.查詢完成,準備轉交(Send the Success view) *************/
				req.setAttribute("orderlistVO", orderlistVO); // 資料庫取出的empVO物件,存入req
				String url = "/Back_end/orderlist/listOneorderlist.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url); // 成功轉交 listOneEmp.jsp
				successView.forward(req, res);

				/*************************** 其他可能的錯誤處理 *************************************/
			} catch (Exception e) {
				errorMsgs.add("無法取得資料:" + e.getMessage());
				RequestDispatcher failureView = req.getRequestDispatcher("/Back_end/orderlist/select_page.jsp");
				failureView.forward(req, res);
			}
		}

		if ("getOne_For_Update".equals(action)) { // 來自listAllEmp.jsp的請求

			List<String> errorMsgs = new LinkedList<String>();
			// Store this set in the request scope, in case we need to
			// send the ErrorPage view.
			req.setAttribute("errorMsgs", errorMsgs);

			try {
				/*************************** 1.接收請求參數 ****************************************/
				String o_id = new String(req.getParameter("o_id"));

				/*************************** 2.開始查詢資料 ****************************************/
				OrderlistService orderlistSvc = new OrderlistService();
				OrderlistVO orderlistVO = orderlistSvc.getOneOrderlist(o_id);

				/*************************** 3.查詢完成,準備轉交(Send the Success view) ************/
				req.setAttribute("orderlistVO", orderlistVO); // 資料庫取出的empVO物件,存入req
				String url = "/Back_end/orderlist/update_orderlist_input.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url);// 成功轉交 update_emp_input.jsp
				successView.forward(req, res);

				/*************************** 其他可能的錯誤處理 **********************************/
			} catch (Exception e) {
				errorMsgs.add("無法取得要修改的資料:" + e.getMessage());
				RequestDispatcher failureView = req.getRequestDispatcher("/Back_end/orderlist/select_page.jsp");
				failureView.forward(req, res);
			}
		}

		if ("update".equals(action)) { // 來自update_emp_input.jsp的請求
			List<String> errorMsgs = new LinkedList<String>();
			// Store this set in the request scope, in case we need to
			// send the ErrorPage view.
			req.setAttribute("errorMsgs", errorMsgs);
			try {
				/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 **********************/
				
				String o_id = new String(req.getParameter("o_id").trim());
				java.sql.Timestamp o_date = null;
				try {
					o_date = java.sql.Timestamp.valueOf(req.getParameter("o_date").trim());
				} catch (Exception e) {
					o_date = new java.sql.Timestamp(System.currentTimeMillis());
					errorMsgs.add("請輸入日期!");
				}
				String o_status = req.getParameter("o_status").trim();
				if (o_status == null || o_status.trim().length() == 0) {
					errorMsgs.add("請勿空白");
				}
				java.sql.Timestamp o_shipdate = null;
				try {
					o_shipdate = java.sql.Timestamp.valueOf(req.getParameter("o_shipdate").trim());
				} catch (Exception e) {
					o_shipdate = new java.sql.Timestamp(System.currentTimeMillis());
					errorMsgs.add("請輸入日期!");
				}

				java.sql.Timestamp o_deceiptdate = null;
				try {
					o_deceiptdate = java.sql.Timestamp.valueOf(req.getParameter("o_deceiptdate").trim());
				} catch (Exception e) {
					o_deceiptdate = new java.sql.Timestamp(System.currentTimeMillis());
					errorMsgs.add("請輸入日期!");
				}
				java.sql.Timestamp o_finishdate = null;
				try {
					o_finishdate = java.sql.Timestamp.valueOf(req.getParameter("o_finishdate").trim());
				} catch (Exception e) {
					o_finishdate = new java.sql.Timestamp(System.currentTimeMillis());
					errorMsgs.add("請輸入日期!");
				}

				String o_transport = new String(req.getParameter("o_transport").trim());
				if (o_transport == null || o_transport.trim().length() == 0) {
					errorMsgs.add("運送方式請勿空白");
				}

				String o_address = new String(req.getParameter("o_address").trim());
				if (o_address == null || o_address.trim().length() == 0) {
					errorMsgs.add("地址請勿空白");
				}

				Integer o_total = null;
				try {
					o_total = new Integer(req.getParameter("o_total").trim());
				} catch (NumberFormatException e) {
					o_total = 0;
					errorMsgs.add("請填數字.");
				}

				Integer o_pm = null;
				try {
					o_pm = new Integer(req.getParameter("o_pm").trim());
				} catch (NumberFormatException e) {
					o_pm = 0;
					errorMsgs.add("請填數字.");
				}

				String m_id = new String(req.getParameter("m_id").trim());
				if (m_id == null || m_id.trim().length() == 0) {
					errorMsgs.add("買家請勿空白");
				}
				OrderlistVO orderlistVO = new OrderlistVO();
				orderlistVO.setO_id(o_id);
				orderlistVO.setO_date(o_date);
				orderlistVO.setO_status(o_status);
				orderlistVO.setO_shipdate(o_shipdate);
				orderlistVO.setO_deceiptdate(o_deceiptdate);
				orderlistVO.setO_finishdate(o_finishdate);
				orderlistVO.setO_transport(o_transport);
				orderlistVO.setO_address(o_address);
				orderlistVO.setO_total(o_total);
				orderlistVO.setO_pm(o_pm);
				orderlistVO.setM_id(m_id);
				// Send the use back to the form, if there were errors
				if (!errorMsgs.isEmpty()) {
					req.setAttribute("orderlistVO", orderlistVO); // 含有輸入格式錯誤的empVO物件,也存入req
					RequestDispatcher failureView = req
							.getRequestDispatcher("/Back_end/orderlist/update_orderlist_input.jsp");
					failureView.forward(req, res);
					return; // 程式中斷
				}
				/*************************** 2.開始修改資料 *****************************************/
				OrderlistService orderlistSvc = new OrderlistService();
				orderlistVO = orderlistSvc.updateOrderlistVO(o_id, o_date, o_status, o_shipdate, o_deceiptdate, o_finishdate, o_transport, o_address, o_total, o_pm, m_id);


				
				
				
				/*************************** 3.修改完成,準備轉交(Send the Success view) *************/
				//推播開始--------------------------
				Jedis jedis = new Jedis("localhost", 6379);
				jedis.auth("123456");
				
				ObjectMapper mapper = new ObjectMapper();
				WebSocket ws = new WebSocket();
				BellVO bellVO = new BellVO();
				
				bellVO.setM_id(m_id);
				bellVO.setMessage("訂單狀態已更改");
				
				ws.onMessage(mapper.writeValueAsString(bellVO));
				
				jedis.close();
				//推播結束--------------------------
				
				req.setAttribute("orderlistVO", orderlistVO); // 資料庫update成功後,正確的的empVO物件,存入req
				String url = "/Back_end/orderlist/listOneorderlist.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url); // 修改成功後,轉交listOneEmp.jsp
				successView.forward(req, res);
				/*************************** 其他可能的錯誤處理 *************************************/
			} catch (Exception e) {
				errorMsgs.add("修改資料失敗:" + e.getMessage());
				RequestDispatcher failureView = req
						.getRequestDispatcher("/Back_end/orderlist/update_orderlist_input.jsp");
				failureView.forward(req, res);
			}
		}

		if ("insert".equals(action)) { // 來自addEmp.jsp的請求

			List<String> errorMsgs = new LinkedList<String>();
			// Store this set in the request scope, in case we need to
			// send the ErrorPage view.
			req.setAttribute("errorMsgs", errorMsgs);

			try {
				/*********************** 1.接收請求參數 - 輸入格式的錯誤處理 *************************/
				String o_id = req.getParameter("o_id");
				String o_idReg = "^[(a-zA-Z0-9_)]{5,10}$";
				if (o_id == null || o_id.trim().length() == 0) {
					errorMsgs.add("請勿空白");
				} else if (!o_id.trim().matches(o_idReg)) { // 以下練習正則(規)表示式(regular-expression)
					errorMsgs.add("英文字母、數字, 且長度必需在5到10之間");
				}

				java.sql.Timestamp o_date = null;
				try {
					o_date = java.sql.Timestamp.valueOf(req.getParameter("o_date").trim());
				} catch (IllegalArgumentException e) {
					o_date = new java.sql.Timestamp(System.currentTimeMillis());
					errorMsgs.add("請輸入日期!");
				}

				String o_status = req.getParameter("o_status").trim();
				if (o_status == null || o_status.trim().length() == 0) {
					errorMsgs.add("狀態請勿空白");
				}

				java.sql.Timestamp o_shipdate = null;
				try {
					o_shipdate = java.sql.Timestamp.valueOf(req.getParameter("o_shipdate").trim());
				} catch (IllegalArgumentException e) {
					o_shipdate = new java.sql.Timestamp(System.currentTimeMillis());
					errorMsgs.add("請輸入日期!");
				}

				java.sql.Timestamp o_deceiptdate = null;
				try {
					o_deceiptdate = java.sql.Timestamp.valueOf(req.getParameter("o_deceiptdate").trim());
				} catch (IllegalArgumentException e) {
					o_shipdate = new java.sql.Timestamp(System.currentTimeMillis());
					errorMsgs.add("請輸入日期!");
				}

				java.sql.Timestamp o_finishdate = null;
				try {
					o_finishdate = java.sql.Timestamp.valueOf(req.getParameter("o_finishdate").trim());
				} catch (IllegalArgumentException e) {
					o_finishdate = new java.sql.Timestamp(System.currentTimeMillis());
					errorMsgs.add("請輸入日期!");
				}

				String o_transport = req.getParameter("o_transport").trim();
				if (o_transport == null || o_transport.trim().length() == 0) {
					errorMsgs.add("運送方式請勿空白");
				}

				String o_address = req.getParameter("o_address").trim();
				if (o_address == null || o_address.trim().length() == 0) {
					errorMsgs.add("地址請勿空白");
				}

				Integer o_total = null;
				try {
					o_total = new Integer(req.getParameter("o_total").trim());
				} catch (NumberFormatException e) {
					o_total = 0;
					errorMsgs.add("金額請填數字.");
				}

				Integer o_pm = null;
				try {
					o_pm = new Integer(req.getParameter("o_pm").trim());
				} catch (NumberFormatException e) {
					o_pm = 0;
					errorMsgs.add("堃幣請填數字.");
				}

				String m_id = req.getParameter("m_id").trim();
				if (m_id == null || m_id.trim().length() == 0) {
					errorMsgs.add("買家請勿空白");
				}

//			Integer deptno = new Integer(req.getParameter("deptno").trim());

				OrderlistVO orderlistVO = new OrderlistVO();
				orderlistVO.setO_id(o_id);
				orderlistVO.setO_date(o_date);
				orderlistVO.setO_status(o_status);
				orderlistVO.setO_shipdate(o_shipdate);
				orderlistVO.setO_deceiptdate(o_deceiptdate);
				orderlistVO.setO_finishdate(o_finishdate);
				orderlistVO.setO_transport(o_transport);
				orderlistVO.setO_address(o_address);
				orderlistVO.setO_total(o_total);
				orderlistVO.setO_pm(o_pm);
				orderlistVO.setM_id(m_id);

				// Send the use back to the form, if there were errors
				if (!errorMsgs.isEmpty()) {
					req.setAttribute("orderlistVO", orderlistVO); // 含有輸入格式錯誤的empVO物件,也存入req
					RequestDispatcher failureView = req.getRequestDispatcher("/Back_end/orderlist/addorderlist.jsp");
					failureView.forward(req, res);
					return;
				}

				/*************************** 2.開始新增資料 ***************************************/
				OrderlistService orderlistSvc = new OrderlistService();
				orderlistVO = orderlistSvc.addOrderlistVO(o_date, o_status, o_shipdate, o_deceiptdate, o_finishdate,
						o_transport, o_address, o_total, o_pm, m_id);

				/*************************** 3.新增完成,準備轉交(Send the Success view) ***********/
				String url = "/Back_end/orderlist/listAllOrderlist.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url); // 新增成功後轉交listAllEmp.jsp
				successView.forward(req, res);

				/*************************** 其他可能的錯誤處理 **********************************/
			} catch (Exception e) {
				errorMsgs.add(e.getMessage());
				RequestDispatcher failureView = req.getRequestDispatcher("/Back_end/orderlist/addorderlist.jsp");
				failureView.forward(req, res);
			}
		}

		if ("delete".equals(action)) { // 來自listAllEmp.jsp

			List<String> errorMsgs = new LinkedList<String>();
			// Store this set in the request scope, in case we need to
			// send the ErrorPage view.
			req.setAttribute("errorMsgs", errorMsgs);
			System.out.println("1");
			try {
				/*************************** 1.接收請求參數 ***************************************/
				String o_id = new String(req.getParameter("o_id"));
				System.out.println(o_id);
				/*************************** 2.開始刪除資料 ***************************************/
				OrderlistService orderlistSvc = new OrderlistService();
				orderlistSvc.deleteOrderlist(o_id);
				System.out.println("3");
				/*************************** 3.刪除完成,準備轉交(Send the Success view) ***********/
				String url = "/Back_end/orderlist/listAllOrderlist.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url);// 刪除成功後,轉交回送出刪除的來源網頁
				successView.forward(req, res);
				System.out.println("4");
				/*************************** 其他可能的錯誤處理 **********************************/
			} catch (Exception e) {
				errorMsgs.add("刪除資料失敗:" + e.getMessage());
				RequestDispatcher failureView = req.getRequestDispatcher("/Back_end/orderlist/listAllOrderlist.jsp");
				failureView.forward(req, res);
			}
		}
	}

}
