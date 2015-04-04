package me.pake.push.message;

/**
 * Defined message class.
 * 
 */
public interface Message {
	
	
	// message type 
	
	/**
	 * users message.
	 * 
	 * users who could accept the push message, message,
	 * while wish list is created or replied by some user.
	 * 
	 * it's customed with email or WeChat pusing...
	 * 
	 * @see BackUsersForWishListMessage,
	 * 		NearestUsersForWishListMessage
	 */
	public static int WISH_LIST = 0x0001;
	
	/**
	 * JSON message.
	 * 
	 * push the JSON message to WeChat users, who could accept the push message,
	 * while wish list is created.
	 * 
	 */
	public static int WISH_LIST_WECHAT_JSON = 0x0002;
	
	/**
	 * Get message type.
	 * @return 
	 */
	public int getType();
	
}
