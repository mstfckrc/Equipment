export interface CartLine {
  lineId: string;
  storeId: string;
  storeName: string;
  listingId: string;
  listingName: string;
  category: string;
  dailyPrice: number;
  quantity: number;
  pickupDate: string;
  returnDate: string;
}
