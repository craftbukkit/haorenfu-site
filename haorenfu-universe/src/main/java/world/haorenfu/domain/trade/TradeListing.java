/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                          TRADING SYSTEM
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * In-game trading marketplace for players to exchange items,
 * services, and virtual currency.
 *
 * Features:
 * - Item listings with pricing
 * - Auction system
 * - Trade history
 * - Reputation-based trust scores
 */
package world.haorenfu.domain.trade;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Trade listing entity.
 */
@Entity
@Table(name = "trade_listings")
public class TradeListing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "seller_username", nullable = false)
    private String sellerUsername;

    @Column(nullable = false)
    private String title;

    @Column(length = 5000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingType type = ListingType.FIXED_PRICE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status = ListingStatus.ACTIVE;

    // Pricing
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency = Currency.DIAMOND;

    // Auction specific fields
    @Column(name = "starting_price", precision = 19, scale = 4)
    private BigDecimal startingPrice;

    @Column(name = "current_bid", precision = 19, scale = 4)
    private BigDecimal currentBid;

    @Column(name = "current_bidder_id")
    private UUID currentBidderId;

    @Column(name = "current_bidder_username")
    private String currentBidderUsername;

    @Column(name = "min_bid_increment", precision = 19, scale = 4)
    private BigDecimal minBidIncrement;

    @Column(name = "auction_end_time")
    private Instant auctionEndTime;

    // Item details
    @Column(name = "item_type")
    private String itemType; // Minecraft item ID

    @Column(name = "item_quantity")
    private int itemQuantity = 1;

    @Column(name = "item_nbt", columnDefinition = "TEXT")
    private String itemNbt; // NBT data for special items

    // Images (Base64 encoded screenshots)
    @ElementCollection
    @CollectionTable(name = "listing_images", joinColumns = @JoinColumn(name = "listing_id"))
    @Column(name = "image_data", columnDefinition = "TEXT")
    private Set<String> images = new HashSet<>();

    // Statistics
    @Column(nullable = false)
    private int views = 0;

    @Column(name = "favorite_count", nullable = false)
    private int favoriteCount = 0;

    // Timestamps
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "sold_at")
    private Instant soldAt;

    // Buyer info (when sold)
    @Column(name = "buyer_id")
    private UUID buyerId;

    @Column(name = "buyer_username")
    private String buyerUsername;

    @Column(name = "final_price", precision = 19, scale = 4)
    private BigDecimal finalPrice;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Constructors
    public TradeListing() {}

    public TradeListing(UUID sellerId, String sellerUsername, String title,
                        ListingCategory category, BigDecimal price) {
        this.sellerId = sellerId;
        this.sellerUsername = sellerUsername;
        this.title = title;
        this.category = category;
        this.price = price;
    }

    // Business methods
    public void incrementViews() {
        this.views++;
    }

    public void addToFavorites() {
        this.favoriteCount++;
    }

    public void removeFromFavorites() {
        if (this.favoriteCount > 0) this.favoriteCount--;
    }

    public boolean placeBid(UUID bidderId, String bidderUsername, BigDecimal bidAmount) {
        if (type != ListingType.AUCTION) return false;
        if (status != ListingStatus.ACTIVE) return false;
        if (auctionEndTime != null && Instant.now().isAfter(auctionEndTime)) return false;

        BigDecimal minimumBid = currentBid != null
            ? currentBid.add(minBidIncrement != null ? minBidIncrement : BigDecimal.ONE)
            : startingPrice;

        if (bidAmount.compareTo(minimumBid) < 0) return false;

        this.currentBid = bidAmount;
        this.currentBidderId = bidderId;
        this.currentBidderUsername = bidderUsername;
        return true;
    }

    public void markAsSold(UUID buyerId, String buyerUsername, BigDecimal finalPrice) {
        this.status = ListingStatus.SOLD;
        this.buyerId = buyerId;
        this.buyerUsername = buyerUsername;
        this.finalPrice = finalPrice;
        this.soldAt = Instant.now();
    }

    public void cancel() {
        this.status = ListingStatus.CANCELLED;
    }

    public void expire() {
        this.status = ListingStatus.EXPIRED;
    }

    public boolean isActive() {
        if (status != ListingStatus.ACTIVE) return false;
        if (type == ListingType.AUCTION && auctionEndTime != null) {
            return Instant.now().isBefore(auctionEndTime);
        }
        return true;
    }

    // Getters and setters
    public UUID getId() { return id; }

    public UUID getSellerId() { return sellerId; }
    public String getSellerUsername() { return sellerUsername; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ListingType getType() { return type; }
    public void setType(ListingType type) { this.type = type; }

    public ListingCategory getCategory() { return category; }
    public void setCategory(ListingCategory category) { this.category = category; }

    public ListingStatus getStatus() { return status; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }

    public BigDecimal getStartingPrice() { return startingPrice; }
    public void setStartingPrice(BigDecimal startingPrice) { this.startingPrice = startingPrice; }

    public BigDecimal getCurrentBid() { return currentBid; }
    public UUID getCurrentBidderId() { return currentBidderId; }
    public String getCurrentBidderUsername() { return currentBidderUsername; }

    public BigDecimal getMinBidIncrement() { return minBidIncrement; }
    public void setMinBidIncrement(BigDecimal increment) { this.minBidIncrement = increment; }

    public Instant getAuctionEndTime() { return auctionEndTime; }
    public void setAuctionEndTime(Instant time) { this.auctionEndTime = time; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public int getItemQuantity() { return itemQuantity; }
    public void setItemQuantity(int quantity) { this.itemQuantity = quantity; }

    public String getItemNbt() { return itemNbt; }
    public void setItemNbt(String nbt) { this.itemNbt = nbt; }

    public Set<String> getImages() { return images; }
    public void setImages(Set<String> images) { this.images = images; }

    public int getViews() { return views; }
    public int getFavoriteCount() { return favoriteCount; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getSoldAt() { return soldAt; }

    public UUID getBuyerId() { return buyerId; }
    public String getBuyerUsername() { return buyerUsername; }
    public BigDecimal getFinalPrice() { return finalPrice; }

    /**
     * Listing types.
     */
    public enum ListingType {
        FIXED_PRICE("一口价", "Fixed price listing"),
        AUCTION("拍卖", "Auction with bidding"),
        TRADE("以物换物", "Item-for-item trade"),
        SERVICE("服务", "Service offering");

        private final String displayName;
        private final String description;

        ListingType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }

    /**
     * Listing categories.
     */
    public enum ListingCategory {
        TOOLS("工具", "Pickaxes, shovels, axes"),
        WEAPONS("武器", "Swords, bows, tridents"),
        ARMOR("护甲", "Helmets, chestplates, etc."),
        BLOCKS("方块", "Building blocks"),
        REDSTONE("红石", "Redstone components"),
        POTIONS("药水", "Potions and effects"),
        FOOD("食物", "Food items"),
        MATERIALS("材料", "Raw materials"),
        ENCHANTED("附魔物品", "Enchanted items"),
        RARE("稀有物品", "Rare and special items"),
        BUILDING_SERVICE("建筑服务", "Building services"),
        REDSTONE_SERVICE("红石服务", "Redstone engineering"),
        OTHER("其他", "Miscellaneous");

        private final String displayName;
        private final String description;

        ListingCategory(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }

    /**
     * Listing status.
     */
    public enum ListingStatus {
        ACTIVE("进行中"),
        SOLD("已售出"),
        CANCELLED("已取消"),
        EXPIRED("已过期");

        private final String displayName;

        ListingStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    /**
     * In-game currencies.
     */
    public enum Currency {
        DIAMOND("钻石", "💎", 1.0),
        EMERALD("绿宝石", "💚", 0.5),
        GOLD("金锭", "🥇", 0.1),
        IRON("铁锭", "🔩", 0.01),
        REPUTATION("声望", "⭐", 0.0); // Cannot be traded directly

        private final String displayName;
        private final String emoji;
        private final double baseValue; // Relative value to diamond

        Currency(String displayName, String emoji, double baseValue) {
            this.displayName = displayName;
            this.emoji = emoji;
            this.baseValue = baseValue;
        }

        public String getDisplayName() { return displayName; }
        public String getEmoji() { return emoji; }
        public double getBaseValue() { return baseValue; }
    }
}
