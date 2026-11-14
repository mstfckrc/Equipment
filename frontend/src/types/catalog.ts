export interface PublicProduct {
  productId: string;
  storeId: string;
  name: string;
  category: string;
  description: string;
  imageUrl: string | null;
  dailyPrice: number;
}

export type ProductStatus = "DRAFT" | "PUBLISHED" | "SUSPENDED" | "ARCHIVED";

export interface StoreProduct extends PublicProduct {
  status: ProductStatus;
  moderationReason: string | null;
  moderatedBy: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ProductListingRequest {
  name: string;
  category: string;
  description: string;
  imageUrl: string | null;
  dailyPrice: number;
}
